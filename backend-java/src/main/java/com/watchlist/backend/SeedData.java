package com.watchlist.backend;

import com.watchlist.backend.model.Show;

import java.util.List;

public final class SeedData {
    private static final String BASE_TIMESTAMP = "2026-04-28T12:00:00.000Z";

    private SeedData() {
    }

    public static List<Show> createShows() {
        return List.of(
                buildShow("American Psycho", "movie", "completed", "2000",
                        "A wealthy New York investment banker hides his psychopathic tendencies behind a polished facade while indulging in violent fantasies.",
                        "https://image.tmdb.org/t/p/w500/9uGHEgsiUXjCNq8wdq4r49YL8A1.jpg", 1, 1, 6.0),
                buildShow("Breaking Bad Season 1", "series", "watching", "2008",
                        "A high school chemistry teacher turned methamphetamine producer partners with a former student to secure his family's future.",
                        "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg", 6, 7, 10.0),
                buildShow("The Lord of the Rings: The Return of the King", "movie", "completed", "2003",
                        "The final battle for Middle-earth begins as Frodo and Sam approach Mount Doom to destroy the One Ring.",
                        "https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg", 1, 1, 10.0),
                buildShow("Better Call Saul Season 1", "series", "watching", "2015",
                        "The transformation of Jimmy McGill into the morally challenged lawyer Saul Goodman begins.",
                        "https://m.media-amazon.com/images/M/MV5BNDdjNTEzMjMtYjM3Mi00NzQ3LWFlNWMtZjdmYWU3ZDkzMjk1XkEyXkFqcGc@._V1_.jpg", 5, 10, 9.0),
                buildShow("Oppenheimer", "movie", "completed", "2023",
                        "The story of J. Robert Oppenheimer and the creation of the atomic bomb during World War II.",
                        "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/Oppenheimer_%28film%29.jpg/250px-Oppenheimer_%28film%29.jpg", 1, 1, 8.0),
                buildShow("Attack on Titan Season 1", "anime", "on-hold", "2013",
                        "Humans live inside walled cities to protect themselves from giant humanoid Titans that devour people.",
                        "https://m.media-amazon.com/images/M/MV5BZjliODY5MzQtMmViZC00MTZmLWFhMWMtMjMwM2I3OGY1MTRiXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg", 12, 12, 9.0),
                buildShow("Fight Club", "movie", "completed", "1999",
                        "An insomniac office worker forms an underground fight club with a soap salesman.",
                        "https://image.tmdb.org/t/p/w500/bptfVGEQuv6vDTIMVCHjJ9Dz8PX.jpg", 1, 1, 7.0),
                buildShow("One Piece", "anime", "watching", "1999",
                        "Monkey D. Luffy and his pirate crew explore the Grand Line in search of the legendary treasure, One Piece.",
                        "https://image.tmdb.org/t/p/w500/e3NBGiAifW9Xt8xD5tpARskjccO.jpg", 1155, null, 10.0),
                buildShow("Interstellar", "movie", "completed", "2014",
                        "A team of explorers travels through a wormhole in space in an attempt to ensure humanity's survival.",
                        "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", 1, 1, 9.0),
                buildShow("Dark Season 1", "series", "watching", "2017",
                        "A time travel conspiracy unravels in a small German town after children start disappearing.",
                        "https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg", 7, 10, 9.0),
                buildShow("Jujutsu Kaisen", "anime", "watching", "2020",
                        "A high school student joins a secret organization of sorcerers to eliminate cursed spirits.",
                        "https://image.tmdb.org/t/p/w500/fHpKWq9ayzSk8nSwqRuaAUemRKh.jpg", 12, 24, 8.0),
                buildShow("Severance Season 1", "series", "plan-to-watch", "2022",
                        "Employees undergo a procedure that separates their work memories from their personal lives.",
                        "https://m.media-amazon.com/images/M/MV5BZDI5YzJhODQtMzQyNy00YWNmLWIxMjUtNDBjNjA5YWRjMzExXkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg", 0, 9, 9.0),
                buildShow("Blade Runner 2049", "movie", "dropped", "2017",
                        "A young blade runner discovers a secret that could plunge society into chaos.",
                        "https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg", 1, 1, 8.0)
        );
    }

    private static Show buildShow(
            String title,
            String type,
            String status,
            String releaseDate,
            String description,
            String image,
            Integer episodesWatched,
            Integer totalEpisodes,
            Double rating
    ) {
        return new Show(
                null,
                title,
                type,
                status,
                description,
                releaseDate,
                image,
                episodesWatched,
                totalEpisodes,
                rating,
                List.of(),
                BASE_TIMESTAMP,
                BASE_TIMESTAMP
        );
    }
}
