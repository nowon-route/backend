package com.example.logindemo.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

//Member 엔티티에 대한 DB처리
public interface MemberRepository extends JpaRepository<Member, Long> {

    //username으로 회원 조회 (Optional -> null 방지)
    Optional<Member> findByUsername(String username);
}
