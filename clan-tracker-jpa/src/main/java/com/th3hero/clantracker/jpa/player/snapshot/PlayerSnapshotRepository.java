package com.th3hero.clantracker.jpa.player.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerSnapshotRepository extends JpaRepository<PlayerSnapshotJpa, PlayerSnapshotKey> {

    @Query("select p from PlayerSnapshotJpa p where p.name like %:name%")
    List<PlayerSnapshotJpa> findByNameContaining(@Param("name") String name);
}
