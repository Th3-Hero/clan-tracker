package com.th3hero.clantracker.jpa.entities;

import com.th3hero.clantracker.api.ui.Rank;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberJpa implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private String name;

    @ManyToOne
    private ClanJpa clanJpa;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column
    private Rank rank;

    @NonNull
    @Column
    private LocalDateTime joinedClan;

    @NonNull
    @Column
    private LocalDateTime lastUpdated;

    public static MemberJpa create(Long id, String name, ClanJpa clanJpa, Rank rank, LocalDateTime joinedClan, LocalDateTime lastUpdated) {
        return MemberJpa.builder()
            .id(id)
            .name(name)
            .clanJpa(clanJpa)
            .rank(rank)
            .joinedClan(joinedClan)
            .lastUpdated(lastUpdated)
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
        return Objects.equals(id, memberJpa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
