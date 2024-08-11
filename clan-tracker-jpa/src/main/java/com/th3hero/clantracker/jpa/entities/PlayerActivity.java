package com.th3hero.clantracker.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "player_activity")
@IdClass(PlayerActivityKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerActivity implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "player_id")
    private Long playerId;

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "fetch_time")
    private LocalDateTime fetchTime;

    @NonNull
    @Column
    private LocalDateTime lastBattle;

    @NonNull
    @Column
    private Long totalRandomBattles;

    @NonNull
    @Column
    private Long totalSkirmishBattles;

    @NonNull
    @Column
    private Long totalAdvancesBattles;

    @NonNull
    @Column
    private Long totalClanWarBattles;

}

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
class PlayerActivityKey implements Serializable {
    private Long playerId;
    private LocalDateTime fetchTime;
}
