package com.th3hero.clantracker.jpa.repositories;

import com.th3hero.clantracker.jpa.entities.ClanJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClanRepository extends JpaRepository<ClanJpa, Long> {

    Optional<ClanJpa> findByTag(String tag);

}