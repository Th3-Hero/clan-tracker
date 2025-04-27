package com.th3hero.clantracker.jpa.player.snapshot;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.jpa.TestEntities;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import com.th3hero.clantracker.jpa.player.PlayerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerSnapshotRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private ClanRepository clanRepository;
    @Autowired
    private PlayerSnapshotRepository playerSnapshotRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findByNameContaining_exact() {
        var clan = TestEntities.clanJpa(1);
        var playerOne = TestEntities.playerJpa(1);
        playerOne.setName("Bob");
        var playerTwo = TestEntities.playerJpa(2);
        playerTwo.setName("Alice");
        clanRepository.saveAndFlush(clan);
        playerOne = playerRepository.saveAndFlush(playerOne);
        playerTwo = playerRepository.saveAndFlush(playerTwo);

        var playerSnapshotOne = playerSnapshotJpa(1, playerOne, clan);
        var playerSnapshotTwo = playerSnapshotJpa(2, playerTwo, clan);

        playerSnapshotRepository.saveAllAndFlush(Arrays.asList(playerSnapshotOne, playerSnapshotTwo));
        entityManager.clear();

        var playerSnapshots = playerSnapshotRepository.findByNameContaining(playerTwo.getName().substring(0, 3));

        assertThat(playerSnapshots).hasSize(1);
        var retrievedSnapshot = playerSnapshots.getFirst();

        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getName)
            .isEqualTo(playerTwo.getName());
        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getPlayerJpa)
            .isEqualTo(playerTwo);
        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getClanJpa)
            .isEqualTo(clan);
    }

    @Test
    void findByNameContaining_missmatchCase() {
        var clan = TestEntities.clanJpa(1);
        var playerOne = TestEntities.playerJpa(1);
        playerOne.setName("Bob");
        var playerTwo = TestEntities.playerJpa(2);
        playerTwo.setName("Alice");
        clanRepository.saveAndFlush(clan);
        playerOne = playerRepository.saveAndFlush(playerOne);
        playerTwo = playerRepository.saveAndFlush(playerTwo);

        var playerSnapshotOne = playerSnapshotJpa(1, playerOne, clan);
        var playerSnapshotTwo = playerSnapshotJpa(2, playerTwo, clan);

        playerSnapshotRepository.saveAllAndFlush(Arrays.asList(playerSnapshotOne, playerSnapshotTwo));
        entityManager.clear();

        var playerSnapshots = playerSnapshotRepository.findByNameContaining("aLiCE".substring(0, 3));

        assertThat(playerSnapshots).hasSize(1);
        var retrievedSnapshot = playerSnapshots.getFirst();

        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getName)
            .isEqualTo(playerTwo.getName());
        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getPlayerJpa)
            .isEqualTo(playerTwo);
        assertThat(retrievedSnapshot).extracting(PlayerSnapshotJpa::getClanJpa)
            .isEqualTo(clan);
    }

    @Test
    void findByNameContaining_multipleContaining() {
        var clan = TestEntities.clanJpa(1);
        var playerOne = TestEntities.playerJpa(1);
        playerOne.setName("Bob");
        var playerTwo = TestEntities.playerJpa(2);
        playerTwo.setName("Bobby");
        clanRepository.saveAndFlush(clan);
        playerOne = playerRepository.saveAndFlush(playerOne);
        playerTwo = playerRepository.saveAndFlush(playerTwo);

        var playerSnapshotOne = playerSnapshotJpa(1, playerOne, clan);
        var playerSnapshotTwo = playerSnapshotJpa(2, playerTwo, clan);

        playerSnapshotRepository.saveAllAndFlush(Arrays.asList(playerSnapshotOne, playerSnapshotTwo));
        entityManager.clear();

        var playerSnapshots = playerSnapshotRepository.findByNameContaining("Bob");

        assertThat(playerSnapshots).hasSize(2);
        assertThat(playerSnapshots).extracting(PlayerSnapshotJpa::getName).containsExactlyInAnyOrder("Bob", "Bobby");
    }

    @Test
    void findByNameContaining_nonContaining() {
        var clan = TestEntities.clanJpa(1);
        var playerOne = TestEntities.playerJpa(1);
        playerOne.setName("Bob");
        var playerTwo = TestEntities.playerJpa(2);
        playerTwo.setName("Alice");
        clanRepository.saveAndFlush(clan);
        playerOne = playerRepository.saveAndFlush(playerOne);
        playerTwo = playerRepository.saveAndFlush(playerTwo);

        var playerSnapshotOne = playerSnapshotJpa(1, playerOne, clan);
        var playerSnapshotTwo = playerSnapshotJpa(2, playerTwo, clan);

        playerSnapshotRepository.saveAllAndFlush(Arrays.asList(playerSnapshotOne, playerSnapshotTwo));
        entityManager.clear();

        var playerSnapshots = playerSnapshotRepository.findByNameContaining("Charlie");

        assertThat(playerSnapshots).isEmpty();
    }

    private static PlayerSnapshotJpa playerSnapshotJpa(int seed, PlayerJpa playerJpa, ClanJpa clanJpa) {
        var member = MemberJpa.builder()
            .playerJpa(playerJpa)
            .clanJpa(clanJpa)
            .rank(Rank.PERSONNEL_OFFICER)
            .joinedClan(LocalDateTime.now().minusMonths(seed + 2))
            .lastUpdated(LocalDateTime.now().minusDays(seed))
            .build();
        clanJpa.getMembers().add(member);
        return PlayerSnapshotJpa.builder()
            .playerJpa(playerJpa)
            .fetchedAt(LocalDateTime.now().minusDays(seed))
            .name(playerJpa.getName())
            .clanJpa(clanJpa)
            .rank(Rank.JUNIOR_OFFICER)
            .joinedAt(LocalDateTime.now().minusMonths(seed))
            .effectiveDate(LocalDate.now().minusDays(seed))
            .effectiveTime(LocalTime.now().minusMinutes(seed))
            .build();
    }
}