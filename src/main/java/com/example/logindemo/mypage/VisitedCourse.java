package com.example.logindemo.mypage;

import com.example.logindemo.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter @Setter
public class VisitedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인한 사용자와 연관 (FK: member_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 코스 식별자 (별도 엔티티 없다고 가정)
    private Long courseId;

    private LocalDate visitedAt;   // 방문 날짜
    private Integer rating;        // 평점 (nullable)

    @Column(length = 500)
    private String note;           // 짤막 메모

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시각 자동

    /* =========================
       폴더 연관관계 (다대다, 정석)
       ========================= */
    @ManyToMany
    @JoinTable(
            name = "folder_item",
            joinColumns = @JoinColumn(name = "visited_course_id"),
            inverseJoinColumns = @JoinColumn(name = "folder_id")
    )
    @OrderBy("name asc")
    private Set<Folder> folders = new LinkedHashSet<>();

    /** 양방향 편의 메서드 */
    public void addTo(Folder folder) {
        if (folder == null) return;
        this.folders.add(folder);
        folder.getItems().add(this);
    }

    public void removeFrom(Folder folder) {
        if (folder == null) return;
        this.folders.remove(folder);
        folder.getItems().remove(this);
    }
}
