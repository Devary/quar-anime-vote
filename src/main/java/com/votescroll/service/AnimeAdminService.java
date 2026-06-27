package com.votescroll.service;

import com.votescroll.dto.AnimeCreateDto;
import com.votescroll.dto.AnimeDto;
import com.votescroll.entity.Anime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AnimeAdminService {

    public List<AnimeDto> listAll() {
        return Anime.<Anime>listAll(Sort.by("name")).stream().map(AnimeDto::from).toList();
    }

    public AnimeDto getById(String id) {
        return AnimeDto.from(findOrThrow(id));
    }

    @Transactional
    public AnimeDto create(AnimeCreateDto req) {
        if (req.name == null || req.name.isBlank()) {
            throw new WebApplicationException("Anime name is required", Response.Status.BAD_REQUEST);
        }
        String trimmed = req.name.trim();
        if (Anime.count("name", trimmed) > 0) {
            throw new WebApplicationException("Anime already exists: " + trimmed, Response.Status.CONFLICT);
        }
        Anime anime = new Anime(UUID.randomUUID().toString(), trimmed, req.imageUrl);
        anime.persist();
        return AnimeDto.from(anime);
    }

    @Transactional
    public AnimeDto update(String id, AnimeCreateDto req) {
        Anime anime = findOrThrow(id);
        if (req.name != null && !req.name.isBlank()) {
            anime.name = req.name.trim();
        }
        if (req.imageUrl != null) {
            anime.imageUrl = req.imageUrl;
        }
        return AnimeDto.from(anime);
    }

    @Transactional
    public void delete(String id) {
        findOrThrow(id).delete();
    }

    private Anime findOrThrow(String id) {
        return Anime.<Anime>findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Anime not found: " + id));
    }
}
