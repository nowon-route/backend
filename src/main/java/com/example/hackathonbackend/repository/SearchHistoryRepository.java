package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.SearchHistory;
import com.example.hackathonbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // 특정 사용자 검색 기록 조회 (최근순)
    List<SearchHistory> findAllByUserOrderByCreatedAtDesc(User user);

    // 최근 N개 검색 기록 조회
    List<SearchHistory> findTop10ByUserOrderByCreatedAtDesc(User user);

}
