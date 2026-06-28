package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class MultiPollAdminService {

    public List<MultiPollDto> listAll() {
        return MultiPoll.<MultiPoll>listAll().stream().map(MultiPollDto::from).collect(Collectors.toList());
    }

    @Transactional
    public MultiPollDto create(MultiPollCreateDto req) {
        long dup = MultiPoll.count("anime = ?1 AND question = ?2", req.anime, req.question);
        if (dup > 0) throw new ClientErrorException("A multi-poll with the same anime and question already exists", 409);

        MultiPoll mp = MultiPoll.builder()
            .id(UUID.randomUUID().toString())
            .anime(req.anime)
            .question(req.question)
            .groups(new ArrayList<>())
            .build();
        mp.persist();

        Instant transactionNow = Instant.now();

        // First pass: create all groups with UUIDs (no feeder refs yet)
        List<MultiPollGroup> groups = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();

        for (int i = 0; i < req.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = req.groups.get(i);
            String gid = UUID.randomUUID().toString();
            groupIds.add(gid);

            boolean isBracket = g.level > 0;
            List<AnimeCharacter> chars = (!isBracket && g.characterIds != null && !g.characterIds.isEmpty())
                ? resolveCharacters(g.characterIds)
                : new ArrayList<>();

            Instant resolvedStart = (!isBracket && g.startNow) ? transactionNow : g.startDate;

            MultiPollGroup group = MultiPollGroup.builder()
                .id(gid)
                .label(g.label)
                .groupOrder(i)
                .level(g.level)
                .poll(mp)
                .candidates(chars)
                .startDate(resolvedStart)
                .endDate(g.endDate)
                .feederGroupIds(new ArrayList<>())
                .build();
            group.persist();
            groups.add(group);
        }

        // Second pass: wire feeder references for bracket groups
        for (int i = 0; i < req.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = req.groups.get(i);
            if (g.level > 0 && g.feederIndices != null) {
                MultiPollGroup group = groups.get(i);
                List<String> feederIds = g.feederIndices.stream()
                    .map(groupIds::get)
                    .collect(Collectors.toList());
                group.feederGroupIds.addAll(feederIds);
            }
        }

        mp.groups = groups;
        log.info("Created multi-poll: {} ({} groups, bracket={})", mp.id, groups.size(),
            groups.stream().anyMatch(g -> g.level > 0));
        return MultiPollDto.from(mp);
    }

    @Transactional
    public MultiPollDto update(String id, MultiPollCreateDto req) {
        MultiPoll mp = MultiPoll.findById(id);
        if (mp == null) throw new NotFoundException("MultiPoll not found: " + id);

        long voteCount = MultiPollVote.count("pollId = ?1", id);
        if (voteCount > 0) throw new ClientErrorException("Cannot update a poll that already has votes", 409);

        int limit = Math.min(req.groups != null ? req.groups.size() : 0, mp.groups.size());
        for (int i = 0; i < limit; i++) {
            MultiPollCreateDto.GroupCreateDto gReq = req.groups.get(i);
            MultiPollGroup group = mp.groups.get(i);
            if (group.level == 0 && gReq.characterIds != null && !gReq.characterIds.isEmpty()) {
                List<AnimeCharacter> chars = resolveCharacters(
                    gReq.characterIds.stream().filter(cid -> cid != null && !cid.isBlank()).collect(Collectors.toList())
                );
                group.candidates.clear();
                group.candidates.addAll(chars);
            }
        }

        log.info("Updated multi-poll candidates: {}", id);
        return MultiPollDto.from(mp);
    }

    @Transactional
    public void delete(String id) {
        MultiPoll mp = MultiPoll.findById(id);
        if (mp == null) throw new NotFoundException("MultiPoll not found: " + id);
        MultiPollVote.delete("pollId", id);
        VoteHistory.delete("pollId", id);
        mp.delete();
        log.info("Deleted multi-poll: {}", id);
    }

    private List<AnimeCharacter> resolveCharacters(List<String> ids) {
        return ids.stream().map(cid -> {
            AnimeCharacter c = AnimeCharacter.findById(cid);
            if (c == null) throw new NotFoundException("Character not found: " + cid);
            return c;
        }).collect(Collectors.toList());
    }
}
