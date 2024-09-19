package com.th3hero.clantracker.jpa.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "player")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerJpa implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private String name;

    public static PlayerJpa create(Long id, String name) {
        return PlayerJpa.builder()
            .id(id)
            .name(name)
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

        PlayerJpa playerJpa = (PlayerJpa) o;
        return id.equals(playerJpa.id) && name.equals(playerJpa.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
