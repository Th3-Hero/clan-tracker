package com.th3hero.clantracker.jpa;

import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClanRepositoryTest {

    @Autowired
    private ClanRepository clanRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findByTag() {
        ClanJpa clanOne = TestEntities.clanJpa(1);
        ClanJpa clanTwo = TestEntities.clanJpa(2);
        ClanJpa clanThree = TestEntities.clanJpa(3);
        clanRepository.saveAllAndFlush(List.of(clanOne, clanTwo, clanThree));
        entityManager.clear();

        Optional<ClanJpa> clan = clanRepository.findByTag(clanTwo.getTag());

        assertThat(clan).contains(clanTwo);
    }

    @Test
    void findByTagReturnsEmptyWhenTagNotFound() {
        ClanJpa clanOne = TestEntities.clanJpa(1);
        ClanJpa clanTwo = TestEntities.clanJpa(2);
        clanRepository.saveAllAndFlush(List.of(clanOne, clanTwo));
        entityManager.clear();

        Optional<ClanJpa> clan = clanRepository.findByTag("nonexistent-tag");

        assertThat(clan).isEmpty();
    }


}
