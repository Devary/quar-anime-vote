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

    public MultiPollResultDto getResult(String pollId, String sessionId) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        return buildResult(poll, sessionId);
    }

    @Transactional
    public MultiPollResultDto vote(String pollId, VoteRequest req) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        validateCandidate(poll, req.characterId);

        if (req.sessionId != null && !req.sessionId.isBlank()) {
            long existing = MultiPollVote.count("pollId = ?1 AND sessionId = ?2", pollId, req.sessionId);
            if (existing > 0) throw new ClientErrorException("Already voted on this poll", Response.Status.CONFLICT);
        }

        MultiPollVote v = MultiPollVote.builder()
            .pollId(pollId).characterId(req.characterId)
            .sessionId(req.sessionId).votedAt(LocalDateTime.now())
            .build();
        v.persist();
        log.info("MultiPoll vote: poll={} char={} session={}", pollId, req.characterId, req.sessionId);
        return buildResult(poll, req.sessionId);
    }

    @Transactional
    public MultiPollResultDto changeVote(String pollId, ChangeVoteRequest req) {
        MultiPoll poll = MultiPoll.findById(pollId);
        if (poll == null) throw new NotFoundException("MultiPoll not found: " + pollId);
        validateCandidate(poll, req.newCharacterId);

        MultiPollVote existing = MultiPollVote
            .find("pollId = ?1 AND sessionId = ?2", pollId, req.sessionId)
            .<MultiPollVote>firstResultOptional()
            .orElseThrow(() -> new NotFoundException("No vote found to change"));
        existing.characterId = req.newCharacterId;
        existing.votedAt = LocalDateTime.now();
        return buildResult(poll, req.sessionId);
    }

    private void validateCandidate(MultiPoll poll, String charId) {
        boolean found = poll.groups.stream()
            .flatMap(g -> g.candidates.stream())
            .anyMatch(c -> c.id.equals(charId));
        if (!found) throw new BadRequestException("Character " + charId + " is not in this multi-poll");
    }

    public MultiPollResultDto buildResult(MultiPoll poll, String sessionId) {
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
        if (sessionId != null && !sessionId.isBlank()) {
            myVote = MultiPollVote.<MultiPollVote>find("pollId = ?1 AND sessionId = ?2", poll.id, sessionId)
                .firstResultOptional().map(v -> v.characterId).orElse(null);
        }

        return MultiPollResultDto.builder()
            .poll(MultiPollDto.from(poll))
            .groups(groupResults)
            .overallWinnerCharId(winner)
            .myVoteCharId(myVote)
            .build();
    }
}
