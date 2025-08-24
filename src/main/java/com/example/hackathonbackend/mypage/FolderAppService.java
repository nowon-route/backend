package com.example.hackathonbackend.mypage;

import com.example.hackathonbackend.member.Member;
import com.example.hackathonbackend.repository.FolderRepository; // ✅ repository 패키지에서 import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.AccessControlException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderAppService {

    private final FolderRepository folderRepository;
    private final VisitedCourseRepository visitedCourseRepository;

    /** 중복 있으면 기존 반환, 없으면 생성 */
    public Folder createIfNotExists(Member owner, String rawName) {
        String name = normalize(rawName);
        return folderRepository.findByOwnerAndName(owner, name)
                .orElseGet(() -> {
                    Folder f = new Folder();
                    f.setOwner(owner);
                    f.setName(name);
                    return folderRepository.save(f);
                });
    }

    @Transactional(readOnly = true)
    public List<Folder> getFoldersByOwner(Member owner) {
        return folderRepository.findByOwnerOrderByNameAsc(owner);
    }

    /** 단건 배정 */
    public void assignOne(Member owner, Long visitedCourseId, Long folderId) {
        VisitedCourse vc = visitedCourseRepository.findById(visitedCourseId).orElseThrow();
        if (!vc.getMember().getId().equals(owner.getId())) throw new AccessControlException("Not yours");
        Folder folder = folderRepository.findById(folderId).orElseThrow();
        if (!folder.getOwner().getId().equals(owner.getId())) throw new AccessControlException("Not your folder");
        vc.addTo(folder); // dirty checking
    }

    /** 다건 배정 */
    public void assignBulk(Member owner, List<Long> visitedCourseIds, Long folderId) {
        for (Long id : visitedCourseIds) assignOne(owner, id, folderId);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }
}