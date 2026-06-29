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
            AnimeCharacter votedChar = poll.effectiveFighters().stream()
                .filter(c -> c.id.equals(v.characterId)).findFirst().orElse(null);
            String name     = votedChar != null ? votedChar.name     : v.characterId;
            String imageUrl = votedChar != null ? votedChar.imageUrl : null;
            result.add(HistoryItemDto.builder()
                .pollId(v.pollId).pollType("single")
                .anime(poll.anime).question(poll.question)
                .myVoteCharId(v.characterId).myVoteCharName(name)
                .myVoteCharImageUrl(imageUrl)
                .votedAt(v.votedAt).build());
        }

        // Multi polls — deduplicated by pollId (show the earliest vote per poll)
        List<MultiPollVote> mVotes = voter.isAuthenticated()
            ? MultiPollVote.list("userId = ?1 AND votedAt >= ?2 AND votedAt < ?3", voter.userId(), from, to)
            : MultiPollVote.list("ipAddress = ?1 AND userId IS NULL AND votedAt >= ?2 AND votedAt < ?3", voter.ipAddress(), from, to);

        // Keep only the earliest vote per pollId
        Map<String, MultiPollVote> dedupByPoll = new LinkedHashMap<>();
        for (MultiPollVote v : mVotes) {
            dedupByPoll.merge(v.pollId, v, (existing, newer) ->
                existing.votedAt.isBefore(newer.votedAt) ? existing : newer);
        }

        for (MultiPollVote v : dedupByPoll.values()) {
            MultiPoll poll = MultiPoll.findById(v.pollId);
            if (poll == null) continue;
            AnimeCharacter votedChar = poll.groups.stream()
                .flatMap(g -> g.candidates.stream())
                .filter(c -> c.id.equals(v.characterId))
                .findFirst().orElse(null);
            String name     = votedChar != null ? votedChar.name     : v.characterId;
            String imageUrl = votedChar != null ? votedChar.imageUrl : null;
            result.add(HistoryItemDto.builder()
                .pollId(v.pollId).pollType("multi")
                .anime(poll.anime).question(poll.question)
                .myVoteCharId(v.characterId).myVoteCharName(name)
                .myVoteCharImageUrl(imageUrl)
                .votedAt(v.votedAt).build());
        }

        result.sort(Comparator.comparing(HistoryItemDto::getVotedAt).reversed());
        return result;
    }
}
