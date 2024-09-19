package com.th3hero.clantracker.jpa.player.snapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerSnapshotRepository extends JpaRepository<PlayerSnapshotJpa, PlayerSnapshotKey> {

}
