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
        if (req.fighterIds == null || req.fighterIds.size() < 2 || req.fighterIds.size() > 10) {
            throw new BadRequestException("Must select between 2 and 10 fighters");
        }
        long distinct = req.fighterIds.stream().distinct().count();
        if (distinct < req.fighterIds.size()) {
            throw new BadRequestException("Duplicate fighters are not allowed");
        }

        List<AnimeCharacter> fighters = new ArrayList<>();
        for (String fId : req.fighterIds) {
            AnimeCharacter f = AnimeCharacter.findById(fId);
            if (f == null) throw new NotFoundException("Character not found: " + fId);
            fighters.add(f);
        }

        long dup = Poll.count(
            "(fighter1.id = ?1 AND fighter2.id = ?2) OR (fighter1.id = ?2 AND fighter2.id = ?1)",
            req.fighterIds.get(0), req.fighterIds.get(1));
        if (dup > 0) throw new ClientErrorException("A poll with these fighters already exists", 409);

        Poll p = Poll.builder()
            .id(UUID.randomUUID().toString())
            .anime(req.anime)
            .question(req.question)
            .fighters(fighters)
            .fighter1(fighters.get(0))
            .fighter2(fighters.get(1))
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
        if (req.fighterIds != null && !req.fighterIds.isEmpty()) {
            if (req.fighterIds.size() < 2 || req.fighterIds.size() > 10) {
                throw new BadRequestException("Must select between 2 and 10 fighters");
            }
            long distinct = req.fighterIds.stream().distinct().count();
            if (distinct < req.fighterIds.size()) {
                throw new BadRequestException("Duplicate fighters are not allowed");
            }
            List<AnimeCharacter> fighters = new ArrayList<>();
            for (String fId : req.fighterIds) {
                AnimeCharacter f = AnimeCharacter.findById(fId);
                if (f == null) throw new NotFoundException("Character not found: " + fId);
                fighters.add(f);
            }
            p.fighters.clear();
            p.fighters.addAll(fighters);
            p.fighter1 = fighters.get(0);
            p.fighter2 = fighters.get(1);
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
