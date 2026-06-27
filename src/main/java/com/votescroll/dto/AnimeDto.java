package com.votescroll.dto;

import com.votescroll.entity.Anime;

public record AnimeDto(String id, String name, String imageUrl) {
    public static AnimeDto from(Anime a) {
        return new AnimeDto(a.id, a.name, a.imageUrl);
    }
}
