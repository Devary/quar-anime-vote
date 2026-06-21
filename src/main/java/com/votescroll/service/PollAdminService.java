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
public class PollAdminService {

    public List<PollDto> listAll() {
        return Poll.<Poll>listAll().stream().map(PollDto::from).collect(Collectors.toList());
    }

    public List<CharacterDto> listCharacters() {
        return AnimeCharacter.<AnimeCharacter>listAll().stream()
            .map(CharacterDto::from).collect(Collectors.toList());
    }

    @Transactional
    public PollDto create(PollCreateDto req) {
        AnimeCharacter f1 = AnimeCharacter.findById(req.fighter1Id);
        AnimeCharacter f2 = AnimeCharacter.findById(req.fighter2Id);
        if (f1 == null) throw new NotFoundException("Character not found: " + req.fighter1Id);
        if (f2 == null) throw new NotFoundException("Character not found: " + req.fighter2Id);
        Poll p = Poll.builder()
            .id(UUID.randomUUID().toString())
            .anime(req.anime)
            .question(req.question)
            .fighter1(f1).fighter2(f2)
            .build();
        p.persist();
        log.info("Created poll: {}", p.id);
        return PollDto.from(p);
    }

    @Transactional
    public PollDto update(String id, PollCreateDto req) {
        Poll p = Poll.findById(id);
        if (p == null) throw new NotFoundException("Poll not found: " + id);
        if (req.anime != null) p.anime = req.anime;
        if (req.question != null) p.question = req.question;
        if (req.fighter1Id != null) {
            AnimeCharacter f1 = AnimeCharacter.findById(req.fighter1Id);
            if (f1 == null) throw new NotFoundException("Character not found: " + req.fighter1Id);
            p.fighter1 = f1;
        }
        if (req.fighter2Id != null) {
            AnimeCharacter f2 = AnimeCharacter.findById(req.fighter2Id);
            if (f2 == null) throw new NotFoundException("Character not found: " + req.fighter2Id);
            p.fighter2 = f2;
        }
        return PollDto.from(p);
    }

    @Transactional
    public void delete(String id) {
        Poll p = Poll.findById(id);
        if (p == null) throw new NotFoundException("Poll not found: " + id);
        Vote.delete("pollId", id);
        VoteHistory.delete("pollId", id);
        p.delete();
        log.info("Deleted poll: {}", id);
    }
}
