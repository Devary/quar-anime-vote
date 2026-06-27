package com.votescroll.service;

import com.votescroll.dto.CharacterCreateDto;
import com.votescroll.dto.CharacterDto;
import com.votescroll.entity.AnimeCharacter;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CharacterAdminService {

    public List<CharacterDto> listAll() {
        return AnimeCharacter.<AnimeCharacter>listAll(Sort.by("anime").and("name"))
                .stream().map(CharacterDto::from).toList();
    }

    @Transactional
    public CharacterDto create(CharacterCreateDto req) {
        AnimeCharacter c = AnimeCharacter.builder()
                .id(UUID.randomUUID().toString())
                .name(req.name)
                .title(req.title)
                .anime(req.anime)
                .imageUrl(req.imageUrl)
                .build();
        c.persist();
        return CharacterDto.from(c);
    }

    @Transactional
    public CharacterDto update(String id, CharacterCreateDto req) {
        AnimeCharacter c = findOrThrow(id);
        if (req.name != null) c.name = req.name;
        if (req.title != null) c.title = req.title;
        if (req.anime != null) c.anime = req.anime;
        if (req.imageUrl != null) c.imageUrl = req.imageUrl;
        return CharacterDto.from(c);
    }

    @Transactional
    public void delete(String id) {
        findOrThrow(id).delete();
    }

    private AnimeCharacter findOrThrow(String id) {
        return AnimeCharacter.<AnimeCharacter>findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Character not found: " + id));
    }
}
