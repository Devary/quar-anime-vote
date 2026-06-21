package com.votescroll;

import com.votescroll.entity.AnimeCharacter;
import com.votescroll.entity.Poll;
import com.votescroll.entity.MultiPoll;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(PgTestProfile.class)
class PostgresConnectionTest {

    @Inject
    EntityManager em;

    @Test
    void connectionAndSeedWorks() {
        long chars  = AnimeCharacter.count();
        long polls  = Poll.count();
        long multis = MultiPoll.count();

        System.out.printf("PostgreSQL animevote — characters=%d  polls=%d  multiPolls=%d%n", chars, polls, multis);

        assertTrue(chars  >= 54,  "Expected at least 54 characters, got " + chars);
        assertTrue(polls  >= 150, "Expected at least 150 polls, got "     + polls);
        assertEquals(10,  multis, "Expected exactly 10 multi-polls, got " + multis);
    }

    @Test
    @Transactional
    void canPersistAndQuery() {
        AnimeCharacter test = AnimeCharacter.builder()
            .id("test-pg-char")
            .name("Test Character")
            .title("Test Title")
            .anime("Test Anime")
            .imageUrl("https://placehold.co/100x100/000/fff?text=T")
            .build();
        test.persist();

        AnimeCharacter found = AnimeCharacter.findById("test-pg-char");
        assertNotNull(found);
        assertEquals("Test Character", found.name);

        found.delete();
        assertNull(AnimeCharacter.findById("test-pg-char"));
    }
}
