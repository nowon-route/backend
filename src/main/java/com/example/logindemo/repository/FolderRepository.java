package com.example.logindemo.repository;

import com.example.logindemo.member.Member;
import com.example.logindemo.mypage.Folder;
import com.example.logindemo.mypage.VisitedCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.logindemo.member.Member;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    // 소유자 객체로 조회 (UI에 리스트 뿌릴 때)
    List<Folder> findByOwnerOrderByNameAsc(Member owner);

    // 소유자+이름 고유성 보장과 중복검사
    Optional<Folder> findByOwnerAndName(Member owner, String name);
    boolean existsByOwnerAndName(Member owner, String name);

    // 필요 시 id로도 조회 가능(선택)
    List<Folder> findByOwnerIdOrderByNameAsc(Long ownerId);

    @Query("select vc from VisitedCourse vc join vc.folders f " +
            "where vc.member = :owner and f.id = :folderId " +
            "order by vc.visitedAt desc")
    List<VisitedCourse> findByOwnerAndFolderId(@Param("owner") Member owner, @Param("folderId") Long folderId);
}


