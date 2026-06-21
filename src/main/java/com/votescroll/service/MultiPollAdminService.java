package com.votescroll.service;

import com.votescroll.dto.*;
import com.votescroll.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
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

        List<MultiPollGroup> groups = new ArrayList<>();
        for (int i = 0; i < req.groups.size(); i++) {
            MultiPollCreateDto.GroupCreateDto g = req.groups.get(i);
            List<AnimeCharacter> chars = g.characterIds.stream()
                .map(cid -> {
                    AnimeCharacter c = AnimeCharacter.findById(cid);
                    if (c == null) throw new NotFoundException("Character not found: " + cid);
                    return c;
                }).collect(Collectors.toList());

            MultiPollGroup group = MultiPollGroup.builder()
                .id(UUID.randomUUID().toString())
                .label(g.label)
                .groupOrder(i)
                .poll(mp)
                .candidates(chars)
                .build();
            group.persist();
            groups.add(group);
        }
        mp.groups = groups;

        log.info("Created multi-poll: {}", mp.id);
        return MultiPollDto.from(mp);
    }

    @Transactional
    public MultiPollDto update(String id, MultiPollCreateDto req) {
        MultiPoll mp = MultiPoll.findById(id);
        if (mp == null) throw new NotFoundException("MultiPoll not found: " + id);
        if (req.anime != null) mp.anime = req.anime;
        if (req.question != null) mp.question = req.question;
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
}
