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
public class PollService {

    public List<PollDto> getAll() {
        return Poll.<Poll>listAll().stream().map(PollDto::from).collect(Collectors.toList());
    }

    public PollResultDto getResult(String pollId, String sessionId) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        return buildResult(poll, sessionId);
    }

    @Transactional
    public PollResultDto vote(String pollId, VoteRequest req) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        validateCharacter(poll, req.characterId);

        if (req.sessionId != null && !req.sessionId.isBlank()) {
            long existing = Vote.count("pollId = ?1 AND sessionId = ?2", pollId, req.sessionId);
            if (existing > 0) throw new ClientErrorException("Already voted on this poll", Response.Status.CONFLICT);
        }

        Vote v = Vote.builder()
            .pollId(pollId)
            .characterId(req.characterId)
            .sessionId(req.sessionId)
            .votedAt(LocalDateTime.now())
            .build();
        v.persist();
        log.info("Vote cast: poll={} char={} session={}", pollId, req.characterId, req.sessionId);
        return buildResult(poll, req.sessionId);
    }

    @Transactional
    public PollResultDto changeVote(String pollId, ChangeVoteRequest req) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        validateCharacter(poll, req.newCharacterId);

        Vote existing = Vote.find("pollId = ?1 AND sessionId = ?2", pollId, req.sessionId)
            .<Vote>firstResultOptional().orElseThrow(() -> new NotFoundException("No vote found to change"));
        existing.characterId = req.newCharacterId;
        existing.votedAt = LocalDateTime.now();
        log.info("Vote changed: poll={} newChar={} session={}", pollId, req.newCharacterId, req.sessionId);
        return buildResult(poll, req.sessionId);
    }

    private void validateCharacter(Poll poll, String charId) {
        if (!poll.fighter1.id.equals(charId) && !poll.fighter2.id.equals(charId)) {
            throw new BadRequestException("Character " + charId + " is not in this poll");
        }
    }

    public PollResultDto buildResult(Poll poll, String sessionId) {
        long c1 = Vote.count("pollId = ?1 AND characterId = ?2", poll.id, poll.fighter1.id);
        long c2 = Vote.count("pollId = ?1 AND characterId = ?2", poll.id, poll.fighter2.id);
        long total = c1 + c2;
        double pct1 = total == 0 ? 50.0 : (c1 * 100.0 / total);
        double pct2 = total == 0 ? 50.0 : (c2 * 100.0 / total);

        String myVote = null;
        if (sessionId != null && !sessionId.isBlank()) {
            myVote = Vote.<Vote>find("pollId = ?1 AND sessionId = ?2", poll.id, sessionId)
                .firstResultOptional().map(v -> v.characterId).orElse(null);
        }

        return PollResultDto.builder()
            .poll(PollDto.from(poll))
            .votes1(c1).votes2(c2).pct1(pct1).pct2(pct2).total(total)
            .myVoteCharId(myVote)
            .build();
    }
}
