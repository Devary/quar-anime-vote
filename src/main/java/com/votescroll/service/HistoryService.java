package com.votescroll.service;

import com.votescroll.dto.HistoryItemDto;
import com.votescroll.dto.VoterIdentity;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.*;

@ApplicationScoped
@Slf4j
public class HistoryService {

    public List<HistoryItemDto> getHistory(VoterIdentity voter, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.plusDays(1).atStartOfDay();

        List<HistoryItemDto> result = new ArrayList<>();

        // Single polls
        List<Vote> votes = voter.isAuthenticated()
            ? Vote.list("userId = ?1 AND votedAt >= ?2 AND votedAt < ?3", voter.userId(), from, to)
            : Vote.list("ipAddress = ?1 AND userId IS NULL AND votedAt >= ?2 AND votedAt < ?3", voter.ipAddress(), from, to);

        for (Vote v : votes) {
            Poll poll = Poll.findById(v.pollId);
            if (poll == null) continue;
            String name = poll.fighter1.id.equals(v.characterId)
                ? poll.fighter1.name : poll.fighter2.name;
            result.add(HistoryItemDto.builder()
                .pollId(v.pollId).pollType("single")
                .anime(poll.anime).question(poll.question)
                .myVoteCharId(v.characterId).myVoteCharName(name)
                .votedAt(v.votedAt).build());
        }

        // Multi polls
        List<MultiPollVote> mVotes = voter.isAuthenticated()
            ? MultiPollVote.list("userId = ?1 AND votedAt >= ?2 AND votedAt < ?3", voter.userId(), from, to)
            : MultiPollVote.list("ipAddress = ?1 AND userId IS NULL AND votedAt >= ?2 AND votedAt < ?3", voter.ipAddress(), from, to);

        for (MultiPollVote v : mVotes) {
            MultiPoll poll = MultiPoll.findById(v.pollId);
            if (poll == null) continue;
            String name = poll.groups.stream()
                .flatMap(g -> g.candidates.stream())
                .filter(c -> c.id.equals(v.characterId))
                .findFirst().map(c -> c.name).orElse(v.characterId);
            result.add(HistoryItemDto.builder()
                .pollId(v.pollId).pollType("multi")
                .anime(poll.anime).question(poll.question)
                .myVoteCharId(v.characterId).myVoteCharName(name)
                .votedAt(v.votedAt).build());
        }

        result.sort(Comparator.comparing(HistoryItemDto::getVotedAt).reversed());
        return result;
    }
}
