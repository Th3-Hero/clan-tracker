package com.th3hero.clantracker.jpa.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerJpa, Long> {

    @Query("SELECT p FROM PlayerJpa p WHERE p.id NOT IN (SELECT m.playerJpa.id FROM MemberJpa m)")
    List<PlayerJpa> findPlayersWithoutClan();
}
