package com.example.logindemo.mypage;

import com.example.logindemo.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitedCourseRepository extends JpaRepository<VisitedCourse, Long> {
    // member 기준 최신 방문 기록
    List<VisitedCourse> findAllByMemberOrderByVisitedAtDesc(Member member);

    // 또는 ID로 찾고 싶다면 아래 중 하나를 쓰세요 (위와 둘 중 하나만 유지)
    // List<VisitedCourse> findAllByMember_IdOrderByVisitedAtDesc(int memberId);
}
