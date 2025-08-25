package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 카카오 ID로 사용자 조회
    User findByKakaoId(String kakaoId);
}
