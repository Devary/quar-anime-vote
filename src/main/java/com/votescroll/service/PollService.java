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

    public PollResultDto getResult(String pollId, VoterIdentity voter) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        return buildResult(poll, voter);
    }

    @Transactional
    public PollResultDto vote(String pollId, VoteRequest req, VoterIdentity voter) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        validateCharacter(poll, req.characterId);

        boolean alreadyVoted = voter.isAuthenticated()
            ? Vote.count("pollId = ?1 AND userId = ?2", pollId, voter.userId()) > 0
            : Vote.count("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", pollId, voter.ipAddress()) > 0;
        if (alreadyVoted) throw new ClientErrorException("Already voted", 409);

        Vote v = Vote.builder()
            .pollId(pollId)
            .characterId(req.characterId)
            .sessionId(req.sessionId)
            .userId(voter.userId())
            .ipAddress(voter.ipAddress())
            .votedAt(LocalDateTime.now())
            .build();
        v.persist();

        VoteHistory.builder()
            .pollId(pollId).pollType("single")
            .charId(req.characterId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .action("VOTE")
            .build().persist();

        log.info("Vote cast: poll={} char={} voter={}", pollId, req.characterId, voter.voterKey());
        return buildResult(poll, voter);
    }

    @Transactional
    public PollResultDto changeVote(String pollId, ChangeVoteRequest req, VoterIdentity voter) {
        Poll poll = Poll.findById(pollId);
        if (poll == null) throw new NotFoundException("Poll not found: " + pollId);
        validateCharacter(poll, req.newCharacterId);

        Vote existing = voter.isAuthenticated()
            ? Vote.find("pollId = ?1 AND userId = ?2", pollId, voter.userId()).<Vote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found"))
            : Vote.find("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", pollId, voter.ipAddress()).<Vote>firstResultOptional()
                .orElseThrow(() -> new NotFoundException("No vote found"));

        existing.characterId = req.newCharacterId;
        existing.votedAt = LocalDateTime.now();

        VoteHistory.builder()
            .pollId(pollId).pollType("single")
            .charId(req.newCharacterId)
            .userId(voter.userId()).ipAddress(voter.ipAddress())
            .action("CHANGE_VOTE")
            .build().persist();

        log.info("Vote changed: poll={} newChar={} voter={}", pollId, req.newCharacterId, voter.voterKey());
        return buildResult(poll, voter);
    }

    private void validateCharacter(Poll poll, String charId) {
        if (!poll.fighter1.id.equals(charId) && !poll.fighter2.id.equals(charId)) {
            throw new BadRequestException("Character " + charId + " is not in this poll");
        }
    }

    public PollResultDto buildResult(Poll poll, VoterIdentity voter) {
        long c1 = Vote.count("pollId = ?1 AND characterId = ?2", poll.id, poll.fighter1.id);
        long c2 = Vote.count("pollId = ?1 AND characterId = ?2", poll.id, poll.fighter2.id);
        long total = c1 + c2;
        double pct1 = total == 0 ? 50.0 : (c1 * 100.0 / total);
        double pct2 = total == 0 ? 50.0 : (c2 * 100.0 / total);

        String myVote = null;
        if (voter != null) {
            if (voter.isAuthenticated()) {
                myVote = Vote.<Vote>find("pollId = ?1 AND userId = ?2", poll.id, voter.userId())
                    .firstResultOptional().map(v -> v.characterId).orElse(null);
            } else if (voter.ipAddress() != null) {
                myVote = Vote.<Vote>find("pollId = ?1 AND ipAddress = ?2 AND userId IS NULL", poll.id, voter.ipAddress())
                    .firstResultOptional().map(v -> v.characterId).orElse(null);
            }
        }

        return PollResultDto.builder()
            .poll(PollDto.from(poll))
            .votes1(c1).votes2(c2).pct1(pct1).pct2(pct2).total(total)
            .myVoteCharId(myVote)
            .build();
    }
}
