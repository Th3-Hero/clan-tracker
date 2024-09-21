package com.th3hero.clantracker.jpa.player.snapshot;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerSnapshotRepositoryTest {

    @Autowired
    private PlayerSnapshotRepository playerSnapshotRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findByNameContaining() {

    }

    @Test
    void findByNameContaining_multipleContaining() {

    }

    @Test
    void findByNameContaining_nonContaining() {

    }
}