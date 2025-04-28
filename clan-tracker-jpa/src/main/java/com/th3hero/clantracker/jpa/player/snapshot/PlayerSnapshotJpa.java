package com.th3hero.clantracker.jpa.player.snapshot;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
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
@Table(name = "player_snapshot")
@IdClass(PlayerSnapshotKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerSnapshotJpa implements Serializable {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    @NonNull
    @Setter(AccessLevel.NONE)
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
    private String name;

    @ManyToOne
    @JoinColumn(name = "clan_id")
    @NonNull
    private ClanJpa clanJpa;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column
    private Rank rank;

    @NonNull
    @Column
    private LocalDateTime joinedAt;

    public static PlayerSnapshotJpa create(
        PlayerJpa playerJpa,
        LocalDateTime fetchedAt,
        ClanJpa clanJpa,
        String name,
        Rank rank,
        LocalDateTime joinedClan,
        LocalDate effectiveDate
    ) {
        return PlayerSnapshotJpa.builder()
            .playerJpa(playerJpa)
            .fetchedAt(fetchedAt)
            .clanJpa(clanJpa)
            .name(name)
            .rank(rank)
            .joinedAt(joinedClan)
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

        PlayerSnapshotJpa that = (PlayerSnapshotJpa) o;
        return playerJpa.equals(that.playerJpa) && fetchedAt.equals(that.fetchedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerJpa, fetchedAt);
    }
}

