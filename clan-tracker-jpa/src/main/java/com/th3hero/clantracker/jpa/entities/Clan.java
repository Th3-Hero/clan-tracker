package com.th3hero.clantracker.jpa.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString(exclude = "members")
@Table(name = "clan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Clan implements Serializable {

    @Id
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private String name;

    @Setter(AccessLevel.NONE)
    @Builder.Default
    @ManyToMany
    private List<Player> members = new ArrayList<>();
}
