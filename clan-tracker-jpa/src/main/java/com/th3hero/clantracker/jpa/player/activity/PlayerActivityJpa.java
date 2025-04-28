package com.th3hero.clantracker.jpa.player.activity;

import com.th3hero.clantracker.jpa.player.PlayerJpa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "player_activity")
@IdClass(PlayerActivityKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerActivityJpa implements Serializable {

    @Id
    @NonNull
    @ManyToOne
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "player_id")
    private PlayerJpa playerJpa;

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    private LocalDateTime fetchedAt;

    @NonNull
    @Column
    private LocalDate effectiveDate;

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

    public static PlayerActivityJpa create(
        PlayerJpa playerJpa,
        LocalDateTime fetchedAt,
        LocalDateTime lastBattle,
        Long totalRandomBattles,
        Long totalSkirmishBattles,
        Long totalAdvancesBattles,
        Long totalClanWarBattles,
        LocalDate effectiveDate
    ) {
        return PlayerActivityJpa.builder()
            .playerJpa(playerJpa)
            .fetchedAt(fetchedAt)
            .lastBattle(lastBattle)
            .totalRandomBattles(totalRandomBattles)
            .totalSkirmishBattles(totalSkirmishBattles)
            .totalAdvancesBattles(totalAdvancesBattles)
            .totalClanWarBattles(totalClanWarBattles)
            .effectiveDate(effectiveDate)
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }

        PlayerActivityJpa playerActivityJpa = (PlayerActivityJpa) o;
        return playerJpa.equals(playerActivityJpa.playerJpa) && fetchedAt.equals(playerActivityJpa.fetchedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerJpa, fetchedAt);
    }
}


@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
class PlayerActivityKey implements Serializable {
    private PlayerJpa playerJpa;
    private LocalDateTime fetchedAt;
}