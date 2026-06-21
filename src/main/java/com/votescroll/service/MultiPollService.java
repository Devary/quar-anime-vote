package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class MultiPollService {

    public List<MultiPollDto> getAll() {
        return MultiPoll.<MultiPoll>listAll().stream().map(MultiPollDto::from).collect(Collectors.toList());
    }

    public MultiPollResultDto getResult(String pollId, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        return buildResult(poll, voter);
    }

    @Transactional
    public MultiPollResultDto vote(String pollId, VoteRequest req, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        validateCandidate(poll, req.characterId);

        boolean alreadyVoted = voter.isAuthenticated()
            ? MultiPollVote.count("pollId = ?1 AND userId = ?2", pollId, voter.userId()) > 0
            : MultiPollVote.count("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", pollId, voter.ipAddress()) > 0;
        if (alreadyVoted) throw new ClientErrorException("Already voted", 409);

        MultiPollVote v = MultiPollVote.builder()
            .pollId(pollId).characterId(req.characterId)
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

        log.info("MultiPoll vote: poll={} char={} voter={}", pollId, req.characterId, voter.voterKey());
        return buildResult(poll, voter);
    }

    @Transactional
    public MultiPollResultDto changeVote(String pollId, ChangeVoteRequest req, VoterIdentity voter) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        validateCandidate(poll, req.newCharacterId);

        MultiPollVote existing = voter.isAuthenticated()
            ? MultiPollVote.find("pollId = ?1 AND userId = ?2", pollId, voter.userId()).<MultiPollVote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found"))
            : MultiPollVote.find("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", pollId, voter.ipAddress()).<MultiPollVote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found"));

        existing.characterId = req.newCharacterId;
        existing.votedAt = LocalDateTime.now();

        VoteHistory.builder()
            .pollId(pollId).pollType("multi")
            .charId(req.newCharacterId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .action("CHANGE_VOTE")
            .build().persist();

        log.info("MultiPoll vote changed: poll={} newChar={} voter={}", pollId, req.newCharacterId, voter.voterKey());
        return buildResult(poll, voter);
    }

    private void validateCandidate(MultiPoll poll, String charId) {
        boolean found = poll.groups.stream()
            .flatMap(g -> g.candidates.stream())
            .anyMatch(c -> c.id.equals(charId));
        if (!found) throw new BadRequestException("Character " + charId + " is not in this multi-poll");
    }

    public MultiPollResultDto buildResult(MultiPoll poll, VoterIdentity voter) {
        List<GroupResultDto> groupResults = poll.groups.stream().map(group -> {
            long groupTotal = group.candidates.stream()
                .mapToLong(c -> MultiPollVote.count("pollId = ?1 AND characterId = ?2", poll.id, c.id))
                .sum();
            List<CandidateResultDto> cands = group.candidates.stream().map(c -> {
                long cnt = MultiPollVote.count("pollId = ?1 AND characterId = ?2", poll.id, c.id);
                double pct = groupTotal == 0 ? (100.0 / group.candidates.size()) : (cnt * 100.0 / groupTotal);
                return CandidateResultDto.builder()
                    .charId(c.id).name(c.name).imageUrl(c.imageUrl)
                    .votes(cnt).pct(pct).build();
            }).collect(Collectors.toList());
            return GroupResultDto.builder()
                .id(group.id).label(group.label).candidates(cands).groupTotal(groupTotal).build();
        }).collect(Collectors.toList());

        // Overall winner: candidate with most votes across all groups
        String winner = groupResults.stream()
            .flatMap(g -> g.candidates.stream())
            .max(Comparator.comparingLong(c -> c.votes))
            .map(c -> c.charId).orElse(null);

        String myVote = null;
        if (voter != null) {
            if (voter.isAuthenticated()) {
                myVote = MultiPollVote.<MultiPollVote>find("pollId = ?1 AND userId = ?2", poll.id, voter.userId())
                    .firstResultOptional().map(v -> v.characterId).orElse(null);
            } else if (voter.ipAddress() != null) {
                myVote = MultiPollVote.<MultiPollVote>find("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", poll.id, voter.ipAddress())
                    .firstResultOptional().map(v -> v.characterId).orElse(null);
            }
        }

        return MultiPollResultDto.builder()
            .poll(MultiPollDto.from(poll))
            .groups(groupResults)
            .overallWinnerCharId(winner)
            .myVoteCharId(myVote)
            .build();
    }
}
