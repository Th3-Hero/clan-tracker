package com.th3hero.clantracker.jpa.repositories;

import com.th3hero.clantracker.jpa.entities.MemberJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberJpa, Long> {

}
