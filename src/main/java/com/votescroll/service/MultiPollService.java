package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class MultiPollService {

    public List<MultiPollDto> getAll() {
        return MultiPoll.<MultiPoll>listAll().stream().map(MultiPollDto::from).collect(Collectors.toList());
    }

    @Transactional
    public MultiPollResultDto getResult(String pollId, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        resolveWinners(poll);
        return buildResult(poll, voter);
    }

    @Transactional
    public MultiPollResultDto vote(String pollId, VoteRequest req, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        resolveWinners(poll);
        MultiPollGroup candidateGroup = resolveGroup(poll, req.characterId);
        checkPeriod(candidateGroup);

        // Per-group duplicate check (users can vote once per group across all levels)
        boolean alreadyVoted = voter.isAuthenticated()
            ? MultiPollVote.count("pollId = ?1 AND groupId = ?2 AND userId = ?3",
                pollId, candidateGroup.id, voter.userId()) > 0
            : MultiPollVote.count("pollId = ?1 AND groupId = ?2 AND ipAddress = ?3 AND userId IS NULL",
                pollId, candidateGroup.id, voter.ipAddress()) > 0;
        if (alreadyVoted) throw new ClientErrorException("Already voted in this group", 409);

        MultiPollVote v = MultiPollVote.builder()
            .pollId(pollId).groupId(candidateGroup.id).characterId(req.characterId)
            .sessionId(req.sessionId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .votedAt(LocalDateTime.now())
            .build();
        v.persist();

        VoteHistory.builder()
            .pollId(pollId).pollType("multi")
            .charId(req.characterId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .action("VOTE")
            .build().persist();

        log.info("MultiPoll vote: poll={} group={} char={} voter={}",
            pollId, candidateGroup.id, req.characterId, voter.voterKey());
        return buildResult(poll, voter);
    }

    @Transactional
    public MultiPollResultDto changeVote(String pollId, ChangeVoteRequest req, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        resolveWinners(poll);
        MultiPollGroup candidateGroup = resolveGroup(poll, req.newCharacterId);
        checkPeriod(candidateGroup);

        MultiPollVote existing = voter.isAuthenticated()
            ? MultiPollVote.find("pollId = ?1 AND groupId = ?2 AND userId = ?3",
                pollId, candidateGroup.id, voter.userId()).<MultiPollVote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found in this group"))
            : MultiPollVote.find("pollId = ?1 AND groupId = ?2 AND ipAddress = ?3 AND userId IS NULL",
                pollId, candidateGroup.id, voter.ipAddress()).<MultiPollVote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found in this group"));

        existing.characterId = req.newCharacterId;
        existing.votedAt = LocalDateTime.now();

        VoteHistory.builder()
            .pollId(pollId).pollType("multi")
            .charId(req.newCharacterId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .action("CHANGE_VOTE")
            .build().persist();

        return buildResult(poll, voter);
    }

    // ── Winner resolution ────────────────────────────────────────────────────

    /**
     * For each unresolved bracket group (level > 0, no candidates yet), check if all
     * feeder groups have ended. If so, populate its candidates with the feeder winners.
     */
    void resolveWinners(MultiPoll poll) {
        Instant now = Instant.now();
        boolean changed = false;

        // Sort by level so lower levels are resolved first
        List<MultiPollGroup> byLevel = poll.groups.stream()
            .sorted(Comparator.comparingInt(g -> g.level))
            .collect(Collectors.toList());

        for (MultiPollGroup group : byLevel) {
            if (group.level == 0 || !group.candidates.isEmpty()) continue;
            if (group.feederGroupIds == null || group.feederGroupIds.isEmpty()) continue;

            boolean allFeedersEnded = group.feederGroupIds.stream().allMatch(fid -> {
                MultiPollGroup feeder = findGroupById(poll, fid);
                return feeder != null
                    && feeder.isResolved()
                    && feeder.endDate != null
                    && now.isAfter(feeder.endDate);
            });
            if (!allFeedersEnded) continue;

            List<AnimeCharacter> winners = group.feederGroupIds.stream()
                .map(fid -> getGroupWinner(poll.id, fid, poll.groups))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if (winners.size() == group.feederGroupIds.size()) {
                group.candidates.addAll(winners);
                changed = true;
                log.info("Resolved bracket group {}: candidates={}", group.id,
                    winners.stream().map(c -> c.name).collect(Collectors.joining(", ")));
            }
        }
    }

    private AnimeCharacter getGroupWinner(String pollId, String groupId, List<MultiPollGroup> groups) {
        MultiPollGroup group = findGroupById(groups, groupId);
        if (group == null || group.candidates.isEmpty()) return null;

        // Count votes per candidate
        Map<String, Long> counts = new java.util.HashMap<>();
        for (AnimeCharacter c : group.candidates) {
            counts.put(c.id, MultiPollVote.count(
                "pollId = ?1 AND groupId = ?2 AND characterId = ?3", pollId, groupId, c.id));
        }

        long maxVotes = counts.values().stream().mapToLong(v -> v).max().orElse(0L);
        // No votes or draw → cannot determine winner; parent group stays TBD
        if (maxVotes == 0L) return null;
        long tiedCount = counts.values().stream().filter(v -> v == maxVotes).count();
        if (tiedCount > 1) return null;

        return group.candidates.stream()
            .filter(c -> counts.get(c.id) == maxVotes)
            .findFirst().orElse(null);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private MultiPollGroup resolveGroup(MultiPoll poll, String charId) {
        return poll.groups.stream()
            .filter(g -> g.candidates.stream().anyMatch(c -> c.id.equals(charId)))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Character " + charId + " is not in this multi-poll"));
    }

    private void checkPeriod(MultiPollGroup group) {
        if (group.startDate == null) return;
        Instant now = Instant.now();
        if (now.isBefore(group.startDate))
            throw new ClientErrorException("Voting for this group has not started yet", 403);
        if (group.endDate != null && now.isAfter(group.endDate))
            throw new ClientErrorException("Voting period for this group has ended", 403);
    }

    private MultiPollGroup findGroupById(MultiPoll poll, String id) {
        return findGroupById(poll.groups, id);
    }

    private MultiPollGroup findGroupById(List<MultiPollGroup> groups, String id) {
        return groups.stream().filter(g -> g.id.equals(id)).findFirst().orElse(null);
    }

    // ── Result builder ────────────────────────────────────────────────────────

    public MultiPollResultDto buildResult(MultiPoll poll, VoterIdentity voter) {
        List<GroupResultDto> groupResults = poll.groups.stream().map(group -> {
            long groupTotal = group.candidates.stream()
                .mapToLong(c -> MultiPollVote.count("pollId = ?1 AND groupId = ?2 AND characterId = ?3",
                    poll.id, group.id, c.id))
                .sum();

            List<CandidateResultDto> cands = group.candidates.stream().map(c -> {
                long cnt = MultiPollVote.count("pollId = ?1 AND groupId = ?2 AND characterId = ?3",
                    poll.id, group.id, c.id);
                double pct = groupTotal == 0
                    ? (100.0 / Math.max(group.candidates.size(), 1))
                    : (cnt * 100.0 / groupTotal);
                return CandidateResultDto.builder()
                    .charId(c.id).name(c.name).imageUrl(c.imageUrl)
                    .votes(cnt).pct(pct).build();
            }).collect(Collectors.toList());

            return GroupResultDto.builder()
                .id(group.id).label(group.label)
                .level(group.level)
                .feederGroupIds(group.feederGroupIds != null ? group.feederGroupIds : List.of())
                .resolved(group.isResolved())
                .candidates(cands).groupTotal(groupTotal)
                .build();
        }).collect(Collectors.toList());

        // Overall winner: candidate with most votes across all resolved groups
        String winner = groupResults.stream()
            .flatMap(g -> g.candidates.stream())
            .max(Comparator.comparingLong(c -> c.votes))
            .map(c -> c.charId).orElse(null);

        // Per-group vote map for the current user
        Map<String, String> myVotesByGroup = new HashMap<>();
        if (voter != null) {
            for (MultiPollGroup group : poll.groups) {
                Optional<MultiPollVote> vote = voter.isAuthenticated()
                    ? MultiPollVote.<MultiPollVote>find("pollId = ?1 AND groupId = ?2 AND userId = ?3",
                        poll.id, group.id, voter.userId()).firstResultOptional()
                    : (voter.ipAddress() != null
                        ? MultiPollVote.<MultiPollVote>find("pollId = ?1 AND groupId = ?2 AND ipAddress = ?3 AND userId IS NULL",
                            poll.id, group.id, voter.ipAddress()).firstResultOptional()
                        : Optional.empty());
                vote.ifPresent(v -> myVotesByGroup.put(group.id, v.characterId));
            }
        }

        return MultiPollResultDto.builder()
            .poll(MultiPollDto.from(poll))
            .groups(groupResults)
            .overallWinnerCharId(winner)
            .myVotesByGroup(myVotesByGroup)
            .build();
    }
}
