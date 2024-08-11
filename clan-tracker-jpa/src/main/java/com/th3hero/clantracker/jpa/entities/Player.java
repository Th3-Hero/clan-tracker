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
@Table(name = "player")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Player implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private String name;

    @ManyToOne
    private Clan clan;

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

    public enum Rank {
        COMMANDER,
        EXECUTIVE_OFFICER,
        COMBAT_OFFICER,
        PERSONNEL_OFFICER,
        INTELLIGENCE_OFFICER,
        QUARTERMASTER,
        RECRUITMENT_OFFICER,
        JUNIOR_OFFICER,
        PRIVATE,
        RESERVIST

    }

}
