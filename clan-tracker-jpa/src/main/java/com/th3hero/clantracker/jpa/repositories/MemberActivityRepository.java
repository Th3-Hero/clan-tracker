package com.th3hero.clantracker.jpa.repositories;

import com.th3hero.clantracker.jpa.entities.MemberActivityJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivityJpa, Long>, JpaSpecificationExecutor<MemberActivityJpa> {

}
