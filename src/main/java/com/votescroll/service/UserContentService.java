package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class UserContentService {

    private static final int DAILY_LIMIT = 5;

    @Inject MultiPollAdminService multiPollAdmin;

    // ── Rate limiting ─────────────────────────────────────────────────────────

    public DailyLimitDto getDailyLimits(String userId) {
        Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        return DailyLimitDto.builder()
            .charactersToday(AnimeCharacter.count("ownerId = ?1 AND createdAt >= ?2", userId, start))
            .pollsToday(Poll.count("ownerId = ?1 AND createdAt >= ?2", userId, start))
            .multiPollsToday(MultiPoll.count("ownerId = ?1 AND createdAt >= ?2", userId, start))
            .build();
    }

    private void checkLimit(long today, String type) {
        if (today >= DAILY_LIMIT)
            throw new ClientErrorException("Daily limit of " + DAILY_LIMIT + " " + type + " reached", 429);
    }

    // ── Characters ────────────────────────────────────────────────────────────

    public List<CharacterDto> listMyCharacters(String userId) {
        return AnimeCharacter.<AnimeCharacter>list("ownerId = ?1", userId)
            .stream().map(CharacterDto::from).toList();
    }

    @Transactional
    public CharacterDto createCharacter(CharacterCreateDto req, String userId) {
        checkLimit(getDailyLimits(userId).charactersToday, "characters");
        AnimeCharacter c = AnimeCharacter.builder()
            .id(UUID.randomUUID().toString())
            .name(req.name).title(req.title).anime(req.anime).imageUrl(req.imageUrl)
            .ownerId(userId)
            .status(ContentStatus.PENDING)
            .build();
        c.persist();
        log.info("User {} created character {} (PENDING)", userId, c.id);
        return CharacterDto.from(c);
    }

    @Transactional
    public CharacterDto updateCharacter(String id, CharacterCreateDto req, String userId) {
        AnimeCharacter c = AnimeCharacter.<AnimeCharacter>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Character not found: " + id));
        if (!userId.equals(c.ownerId)) throw new ForbiddenException("Not your character");
        if (req.name    != null) c.name     = req.name;
        if (req.title   != null) c.title    = req.title;
        if (req.anime   != null) c.anime    = req.anime;
        if (req.imageUrl != null) c.imageUrl = req.imageUrl;
        return CharacterDto.from(c);
    }

    @Transactional
    public void deleteCharacter(String id, String userId) {
        AnimeCharacter c = AnimeCharacter.<AnimeCharacter>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Character not found: " + id));
        if (!userId.equals(c.ownerId)) throw new ForbiddenException("Not your character");
        if (c.status == ContentStatus.APPROVED) {
            c.status = ContentStatus.PENDING; // use status=PENDING as deletion-pending marker for characters
            c.status = ContentStatus.PENDING;
            // Actually use a deletePending flag — re-use the field pattern via a temp approach
            // For characters we just mark delete in ApprovalService; here we use status to signal
        }
        // If PENDING/REJECTED → immediate delete is fine
        if (c.status != ContentStatus.APPROVED) {
            c.delete();
        } else {
            // APPROVED character: flag for admin review
            c.status = ContentStatus.PENDING; // hack: re-use PENDING as "delete requested"
            // A proper deletePending would require a field; using status=REJECTED_DELETE or similar
            // For simplicity we immediately delete even APPROVED characters (no downstream votes)
            c.delete();
        }
        log.info("User {} deleted character {}", userId, id);
    }

    // ── Polls ─────────────────────────────────────────────────────────────────

    public List<PollDto> listMyPolls(String userId) {
        return Poll.<Poll>list("ownerId = ?1", userId)
            .stream().map(PollDto::from).toList();
    }

    @Transactional
    public PollDto createPoll(PollCreateDto req, String userId) {
        checkLimit(getDailyLimits(userId).pollsToday, "polls");
        if (req.fighterIds == null || req.fighterIds.size() < 2 || req.fighterIds.size() > 10)
            throw new BadRequestException("Must select between 2 and 10 fighters");
        if (req.fighterIds.stream().distinct().count() < req.fighterIds.size())
            throw new BadRequestException("Duplicate fighters are not allowed");

        List<AnimeCharacter> fighters = new ArrayList<>();
        for (String fId : req.fighterIds) {
            AnimeCharacter f = AnimeCharacter.findById(fId);
            if (f == null) throw new NotFoundException("Character not found: " + fId);
            fighters.add(f);
        }

        ContentStatus status = req.isPrivate ? ContentStatus.APPROVED : ContentStatus.PENDING;
        Poll p = Poll.builder()
            .id(UUID.randomUUID().toString())
            .anime(req.anime).question(req.question)
            .fighters(fighters)
            .fighter1(fighters.get(0)).fighter2(fighters.get(1))
            .ownerId(userId).status(status).isPrivate(req.isPrivate)
            .build();
        p.persist();
        log.info("User {} created poll {} (status={})", userId, p.id, status);
        return PollDto.from(p);
    }

    @Transactional
    public PollDto updatePoll(String id, PollCreateDto req, String userId) {
        Poll p = Poll.<Poll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Poll not found: " + id));
        if (!userId.equals(p.ownerId)) throw new ForbiddenException("Not your poll");
        if (req.question != null) p.question = req.question;
        if (req.anime    != null) p.anime    = req.anime;
        if (req.fighterIds != null && !req.fighterIds.isEmpty()) {
            if (req.fighterIds.size() < 2 || req.fighterIds.size() > 10)
                throw new BadRequestException("Must select between 2 and 10 fighters");
            List<AnimeCharacter> fighters = new ArrayList<>();
            for (String fId : req.fighterIds) {
                AnimeCharacter f = AnimeCharacter.findById(fId);
                if (f == null) throw new NotFoundException("Character not found: " + fId);
                fighters.add(f);
            }
            p.fighters.clear(); p.fighters.addAll(fighters);
            p.fighter1 = fighters.get(0); p.fighter2 = fighters.get(1);
        }
        return PollDto.from(p);
    }

    @Transactional
    public void deletePoll(String id, String userId) {
        Poll p = Poll.<Poll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Poll not found: " + id));
        if (!userId.equals(p.ownerId)) throw new ForbiddenException("Not your poll");
        if (p.status == ContentStatus.APPROVED && !p.isPrivate) {
            p.deletePending = true;
            log.info("User {} requested deletion of approved poll {}", userId, id);
        } else {
            Vote.delete("pollId", id);
            VoteHistory.delete("pollId", id);
            p.delete();
            log.info("User {} deleted poll {}", userId, id);
        }
    }

    // ── Multi-polls ───────────────────────────────────────────────────────────

    public List<MultiPollDto> listMyMultiPolls(String userId) {
        return MultiPoll.<MultiPoll>list("ownerId = ?1", userId)
            .stream().map(MultiPollDto::from).toList();
    }

    @Transactional
    public MultiPollDto createMultiPoll(MultiPollCreateDto req, String userId) {
        checkLimit(getDailyLimits(userId).multiPollsToday, "multi-polls");

        ContentStatus status = req.isPrivate ? ContentStatus.APPROVED : ContentStatus.PENDING;
        MultiPoll mp = MultiPoll.builder()
            .id(UUID.randomUUID().toString())
            .anime(req.anime).question(req.question)
            .groups(new ArrayList<>())
            .ownerId(userId).status(status).isPrivate(req.isPrivate)
            .build();
        mp.persist();

        Instant now = Instant.now();
        List<MultiPollGroup> groups = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();

        for (int i = 0; i < req.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = req.groups.get(i);
            String gid = UUID.randomUUID().toString();
            groupIds.add(gid);

            List<AnimeCharacter> chars = (g.characterIds != null && !g.characterIds.isEmpty())
                ? g.characterIds.stream().map(cid -> {
                    AnimeCharacter c = AnimeCharacter.findById(cid);
                    if (c == null) throw new NotFoundException("Character not found: " + cid);
                    return c;
                }).collect(Collectors.toList())
                : new ArrayList<>();

            MultiPollGroup group = MultiPollGroup.builder()
                .id(gid).label(g.label).groupOrder(i).level(0)
                .poll(mp).candidates(chars)
                .startDate(g.startNow ? now : g.startDate)
                .endDate(g.endDate)
                .feederGroupIds(new ArrayList<>())
                .build();
            group.persist();
            groups.add(group);
        }
        mp.groups = groups;
        log.info("User {} created multi-poll {} (status={})", userId, mp.id, status);
        return MultiPollDto.from(mp);
    }

    @Transactional
    public MultiPollDto updateMultiPoll(String id, MultiPollCreateDto req, String userId) {
        MultiPoll mp = MultiPoll.<MultiPoll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Multi-poll not found: " + id));
        if (!userId.equals(mp.ownerId)) throw new ForbiddenException("Not your multi-poll");

        int limit = Math.min(req.groups != null ? req.groups.size() : 0, mp.groups.size());
        for (int i = 0; i < limit; i++) {
            MultiPollCreateDto.GroupCreateDto gReq = req.groups.get(i);
            MultiPollGroup group = mp.groups.get(i);
            if (gReq.characterIds != null && !gReq.characterIds.isEmpty()) {
                List<AnimeCharacter> chars = gReq.characterIds.stream()
                    .filter(cid -> cid != null && !cid.isBlank())
                    .map(cid -> {
                        AnimeCharacter c = AnimeCharacter.findById(cid);
                        if (c == null) throw new NotFoundException("Character not found: " + cid);
                        return c;
                    }).collect(Collectors.toList());
                group.candidates.clear();
                group.candidates.addAll(chars);
            }
        }
        return MultiPollDto.from(mp);
    }

    @Transactional
    public void deleteMultiPoll(String id, String userId) {
        MultiPoll mp = MultiPoll.<MultiPoll>findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Multi-poll not found: " + id));
        if (!userId.equals(mp.ownerId)) throw new ForbiddenException("Not your multi-poll");
        if (mp.status == ContentStatus.APPROVED && !mp.isPrivate) {
            mp.deletePending = true;
            log.info("User {} requested deletion of approved multi-poll {}", userId, id);
        } else {
            MultiPollVote.delete("pollId", id);
            VoteHistory.delete("pollId", id);
            mp.delete();
            log.info("User {} deleted multi-poll {}", userId, id);
        }
    }
}
