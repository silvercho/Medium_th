package com.ll.medium.domain.member.member.repository;

import com.ll.medium.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    Page<Member> findByUsernameContainsIgnoreCase(String kw, Pageable pageable);
}
