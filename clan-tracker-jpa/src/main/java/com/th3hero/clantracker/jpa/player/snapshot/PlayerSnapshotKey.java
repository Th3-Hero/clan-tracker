package com.th3hero.clantracker.jpa.player.snapshot;

import com.th3hero.clantracker.jpa.player.PlayerJpa;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerSnapshotKey implements Serializable {
    private PlayerJpa playerJpa;
    private LocalDateTime fetchedAt;
}
