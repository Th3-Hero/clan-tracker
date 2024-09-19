package com.th3hero.clantracker.jpa.player.activity;

import com.th3hero.clantracker.jpa.player.PlayerJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerActivityRepository extends JpaRepository<PlayerActivityJpa, PlayerJpa>, JpaSpecificationExecutor<PlayerActivityJpa> {

}
