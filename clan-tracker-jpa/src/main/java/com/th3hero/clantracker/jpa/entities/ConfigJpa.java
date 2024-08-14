package com.th3hero.clantracker.jpa.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfigJpa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_id_seq")
    @SequenceGenerator(name = "config_id_seq", sequenceName = "config_id_seq", allocationSize = 1)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column
    private Integer memberActivityUpdateInterval;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }

        ConfigJpa configJpa = (ConfigJpa) o;
        return Objects.equals(id, configJpa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
