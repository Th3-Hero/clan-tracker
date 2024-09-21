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

        // Save players before creating members
        var playerOne = playerRepository.save(TestEntities.playerJpa(1));
        var playerTwo = playerRepository.save(TestEntities.playerJpa(2));
        var playerThree = playerRepository.save(TestEntities.playerJpa(3));
        playerRepository.flush();

        var memberOne = MemberJpa.create(playerOne, clanOne, Rank.RECRUIT, LocalDateTime.now().minusMonths(4), LocalDateTime.now().minusHours(3));
        var memberTwo = MemberJpa.create(playerTwo, clanOne, Rank.RECRUIT, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusHours(6));
        memberRepository.saveAllAndFlush(List.of(memberOne, memberTwo));
        entityManager.clear();

        var playersWithoutClan = playerRepository.findPlayersWithoutClan();

        assertThat(playersWithoutClan).containsExactly(playerThree);
    }

    @Test
    void findPlayersWithoutClan_allHaveClan() {
        var clanOne = clanRepository.save(TestEntities.clanJpa(1));
        var clanTwo = clanRepository.save(TestEntities.clanJpa(2));
        clanRepository.flush();

        // Save players before creating members
        var playerOne = playerRepository.save(TestEntities.playerJpa(1));
        var playerTwo = playerRepository.save(TestEntities.playerJpa(2));
        playerRepository.flush();

        var memberOne = MemberJpa.create(playerOne, clanOne, Rank.RECRUIT, LocalDateTime.now().minusMonths(4), LocalDateTime.now().minusHours(3));
        var memberTwo = MemberJpa.create(playerTwo, clanTwo, Rank.RECRUIT, LocalDateTime.now().minusMonths(2), LocalDateTime.now().minusHours(6));
        memberRepository.saveAllAndFlush(List.of(memberOne, memberTwo));
        entityManager.clear();

        var playersWithoutClan = playerRepository.findPlayersWithoutClan();

        assertThat(playersWithoutClan).isEmpty();
    }

    @Test
    void findPlayersWithoutClan_noneHaveClan() {
        List<PlayerJpa> players =  playerRepository.saveAllAndFlush(List.of(TestEntities.playerJpa(1), TestEntities.playerJpa(2), TestEntities.playerJpa(3)));

        var playersWithoutClan = playerRepository.findPlayersWithoutClan();

        assertThat(playersWithoutClan)
            .hasSize(3)
            .containsExactlyInAnyOrderElementsOf(players);
    }
}