package com.example.hackathonbackend.mypage;

import com.example.hackathonbackend.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "folder",
        uniqueConstraints = @UniqueConstraint(name = "uk_folder_owner_name", columnNames = {"owner_id", "name"})
)
@Getter @Setter @NoArgsConstructor
public class Folder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private Member owner;

    // 읽기 편의 (주인은 VisitedCourse 쪽)
    @ManyToMany(mappedBy = "folders")
    private Set<VisitedCourse> items = new LinkedHashSet<>();
}