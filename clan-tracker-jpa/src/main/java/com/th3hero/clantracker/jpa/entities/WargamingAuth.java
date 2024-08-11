package com.th3hero.clantracker.jpa.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@ToString
@Table(name = "wg_auth")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WargamingAuth implements Serializable {

    @Id
    @NonNull
    @Setter(AccessLevel.NONE)
    private Long playerId;


}
