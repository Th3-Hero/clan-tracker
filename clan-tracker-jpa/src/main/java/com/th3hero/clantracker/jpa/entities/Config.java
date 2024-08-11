package com.th3hero.clantracker.jpa.entities;

import com.th3hero.clantracker.jpa.entities.Player.Rank;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@ToString(exclude = "ranksAllowedToEdit")
@Table(name = "config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Config implements Serializable {

    @Id
    @NonNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_id_seq")
    @SequenceGenerator(name = "config_id_seq", sequenceName = "config_id_seq", allocationSize = 1)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private Integer performanceThresholdBad;

    @NonNull
    @Column
    private Integer performanceThresholdPoor;

    @NonNull
    @Column
    private Integer performanceThresholdGood;

    @NonNull
    @Column
    private Integer defaultActivitySummaryDateRange;

    @NonNull
    @Column
    private Integer defaultProbationPeriod;

    @NonNull
    @ElementCollection(targetClass = Rank.class)
    @CollectionTable(name = "config_ranks_allowed_to_edit", joinColumns = @JoinColumn(name = "config_id"))
    @Enumerated(EnumType.STRING)
    private List<Rank> ranksAllowedToEdit;
}
