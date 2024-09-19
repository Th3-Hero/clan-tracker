package com.th3hero.clantracker.jpa.clan;

import com.th3hero.clantracker.jpa.member.MemberJpa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString(exclude = "members")
@Table(name = "clan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanJpa implements Serializable {

    @Id
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private String tag;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @OneToMany(mappedBy = "clanJpa", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MemberJpa> members = new ArrayList<>();

    public static ClanJpa create(Long id, String tag) {
        return ClanJpa.builder()
            .id(id)
            .tag(tag)
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

        ClanJpa clanJpa = (ClanJpa) o;
        return Objects.equals(id, clanJpa.id) && tag.equals(clanJpa.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
