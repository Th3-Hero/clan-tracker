package com.th3hero.clantracker.jpa.member;

import com.th3hero.clantracker.api.ui.Rank;
import com.th3hero.clantracker.jpa.clan.ClanJpa;
import com.th3hero.clantracker.jpa.player.PlayerJpa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "member")
@IdClass(MemberJpaKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpa implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "player_id")
    @NonNull
    @Setter(AccessLevel.NONE)
    private PlayerJpa playerJpa;

    @Id
    @ManyToOne
    @JoinColumn(name = "clan_id")
    @NonNull
    @Setter(AccessLevel.NONE)
    private ClanJpa clanJpa;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column
    private Rank rank;

    @NonNull
    @Column
    private LocalDateTime joinedClan;

    public static MemberJpa create(PlayerJpa playerJpa, ClanJpa clanJpa, Rank rank, LocalDateTime joinedClan) {
        return MemberJpa.builder()
            .playerJpa(playerJpa)
            .clanJpa(clanJpa)
            .rank(rank)
            .joinedClan(joinedClan)
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

        MemberJpa memberJpa = (MemberJpa) o;
        return playerJpa.equals(memberJpa.playerJpa) && clanJpa.equals(memberJpa.clanJpa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerJpa, clanJpa);
    }

}

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
class MemberJpaKey implements Serializable {
    private PlayerJpa playerJpa;
    private ClanJpa clanJpa;
}
