package com.th3hero.clantracker.jpa.entities;

import com.th3hero.clantracker.jpa.entities.MemberJpa.Rank;
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
@Table(name = "member_activity")
@IdClass(PlayerActivityJpaKey.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberActivityJpa implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "member_id")
    private Long memberId;

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    @JoinColumn(name = "updated_at")
    private LocalDateTime updatedAt;

    @NonNull
    @Column
    private String name;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column
    private Rank rank;

    @NonNull
    @Column
    private Long clanId;

    @NonNull
    @Column
    private LocalDateTime joinedClan;

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

    public static MemberActivityJpa create(
        Long memberId,
        LocalDateTime updatedAt,
        String name,
        Rank rank,
        Long clanId,
        LocalDateTime joinedClan,
        LocalDateTime lastBattle,
        Long totalRandomBattles,
        Long totalSkirmishBattles,
        Long totalAdvancesBattles,
        Long totalClanWarBattles
    ) {
        return MemberActivityJpa.builder()
            .memberId(memberId)
            .updatedAt(updatedAt)
            .name(name)
            .rank(rank)
            .clanId(clanId)
            .joinedClan(joinedClan)
            .lastBattle(lastBattle)
            .totalRandomBattles(totalRandomBattles)
            .totalSkirmishBattles(totalSkirmishBattles)
            .totalAdvancesBattles(totalAdvancesBattles)
            .totalClanWarBattles(totalClanWarBattles)
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

        MemberActivityJpa memberActivityJpa = (MemberActivityJpa) o;
        return Objects.equals(memberId, memberActivityJpa.memberId) && Objects.equals(updatedAt, memberActivityJpa.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode("%s-%S".formatted(memberId, updatedAt));
    }
}

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
class PlayerActivityJpaKey implements Serializable {
    private Long memberId;
    private LocalDateTime updatedAt;
}
