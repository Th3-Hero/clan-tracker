package com.th3hero.clantracker.jpa.player;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.jpa.TestEntities;
import com.th3hero.clantracker.jpa.clan.ClanRepository;
import com.th3hero.clantracker.jpa.member.MemberJpa;
import com.th3hero.clantracker.jpa.member.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerRepositoryTest {

    @Autowired
    private ClanRepository clanRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findPlayersWithoutClan() {
        var clanOne = TestEntities.clanJpa(1);
        clanOne = clanRepository.saveAndFlush(clanOne);
        entityManager.clear();

        var playerOne = TestEntities.playerJpa(1);
        var playerTwo = TestEntities.playerJpa(2);
        var playerThree = TestEntities.playerJpa(3);

        // Save players before creating members
        playerRepository.saveAllAndFlush(List.of(playerOne, playerTwo, playerThree));
        entityManager.clear();

        var memberOne = MemberJpa.create(playerOne, clanOne, Rank.COMBAT_OFFICER, LocalDateTime.now(), LocalDateTime.now().minusDays(2));
        var memberTwo = MemberJpa.create(playerTwo, clanOne, Rank.COMBAT_OFFICER, LocalDateTime.now(), LocalDateTime.now().minusHours(3));
        memberRepository.saveAllAndFlush(List.of(memberOne));
        entityManager.clear();

        var playersWithoutClan = playerRepository.findPlayersWithoutClan();

        assertThat(playersWithoutClan).containsExactly(playerThree);
    }

    @Test
    void findPlayersWithoutClan_allHaveClan() {

    }

    @Test
    void findPlayersWithoutClan_noneHaveClan() {

    }
}