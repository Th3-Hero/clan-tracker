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
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment implements Serializable {

    @Id
    @NonNull
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_seq")
    @SequenceGenerator(name = "comment_id_seq", sequenceName = "comment_id_seq", allocationSize = 1)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Player author;

    @NonNull
    @Column
    private String content;

    @NonNull
    @Column
    private LocalDateTime datePosted;
}
