package com.example.hackathonbackend.controller;

import com.example.hackathonbackend.dto.ProfileForm;
import com.example.hackathonbackend.member.Member;
import com.example.hackathonbackend.member.MemberRepository;
import com.example.hackathonbackend.mypage.Folder;
import com.example.hackathonbackend.mypage.FolderAppService;
import com.example.hackathonbackend.repository.FolderRepository;
import com.example.hackathonbackend.mypage.VisitedCourse;
import com.example.hackathonbackend.mypage.VisitedCourseRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.AccessControlException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MyPage 화면 컨트롤러 (데모 모드 + 폴더 기능)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/MyPage")
public class MyPageController {

    private final MemberRepository memberRepository;
    private final VisitedCourseRepository visitedCourseRepository;

    // 폴더 기능 주입
    private final FolderRepository folderRepository;
    private final FolderAppService folderAppService;

    /** 로그인 필수 구간에서 사용 */
    private Member requireLogin(HttpSession session) {
        Member login = (Member) session.getAttribute("loggedInMember");
        if (login == null) throw new IllegalStateException("NEED_LOGIN");
        return memberRepository.findById(login.getId()).orElseThrow();
    }

    /** 데모 모드: 세션이 없으면 최신 회원을 하나 골라 반환(없으면 null) */
    private Member resolveMemberOrLatest(HttpSession session) {
        Member login = (Member) session.getAttribute("loggedInMember");
        if (login != null) {
            return memberRepository.findById(login.getId()).orElse(null);
        }
        return memberRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().findFirst().orElse(null);
    }

    /** MyPage: 폴더 필터링 + 폴더 목록 + 저장 항목 목록 */
    @GetMapping
    public String myPage(@RequestParam(required = false) Long folderId,
                         Model model, HttpSession session) {

        // 1) 세션 or 최신 회원(데모)
        Member member = resolveMemberOrLatest(session);
        model.addAttribute("member", member);

        // 2) 내 방문기록
        List<VisitedCourse> allMyRecords;
        if (member != null) {
            allMyRecords = visitedCourseRepository.findAllByMemberOrderByVisitedAtDesc(member);
        } else {
            allMyRecords = visitedCourseRepository.findAll().stream()
                    .sorted(Comparator.comparingLong(VisitedCourse::getId).reversed())
                    .limit(10)
                    .toList();
        }

        // 3) 폴더 목록
        List<Folder> folders = (member != null)
                ? folderRepository.findByOwnerOrderByNameAsc(member)
                : List.of();
        model.addAttribute("folders", folders);

        // 4) 폴더 필터링
        List<VisitedCourse> bases;
        if (member != null && folderId != null) {
            // 간단 버전(인메모리 필터)
            bases = allMyRecords.stream()
                    .filter(vc -> vc.getFolders().stream().anyMatch(f -> Objects.equals(f.getId(), folderId)))
                    .toList();

            // 성능이 필요하면 아래 주석 해제(쿼리로 조회)
            // bases = visitedCourseRepository.findByOwnerAndFolderId(member, folderId);
        } else {
            bases = allMyRecords;
        }

        // 5) 화면 DTO 매핑
        var savedItems = bases.stream().map(vc -> {
            Long id = vc.getId();
            Long courseId = vc.getCourseId();
            String note = vc.getNote();
            Integer rating = vc.getRating();
            LocalDateTime updatedAt = null;
            try {
                var createdAtGetter = vc.getClass().getMethod("getCreatedAt");
                Object val = createdAtGetter.invoke(vc);
                if (val instanceof LocalDateTime ldt) updatedAt = ldt;
            } catch (Exception ignored) {
                if (vc.getVisitedAt() != null) updatedAt = vc.getVisitedAt().atStartOfDay();
            }

            List<String> itemFolders = vc.getFolders().stream()
                    .map(Folder::getName)
                    .toList();

            return new SavedItemView(
                    id,
                    "코스 #" + courseId,
                    (note != null && !note.isBlank()) ? note : "설명이 없습니다.",
                    "https://placehold.co/192x144?text=" + courseId,
                    "공개",
                    "평점 " + (rating != null ? rating : "-"),
                    "/course/" + courseId,
                    updatedAt,
                    itemFolders
            );
        }).toList();

        model.addAttribute("savedItems", savedItems);
        return "MyPage";
    }

    /** 폴더 만들기 */
    @PostMapping("/folders")
    public String createFolder(@RequestParam String name, HttpSession session) {
        Member me = requireLogin(session);
        if (name != null && !name.isBlank()) folderAppService.createIfNotExists(me, name);
        return "redirect:/MyPage";
    }

    /** 단건: 폴더 배정 */
    @PostMapping("/saved/{visitedCourseId}/assign")
    public String assignOne(@PathVariable Long visitedCourseId,
                            @RequestParam Long folderId,
                            HttpSession session) {
        Member me = requireLogin(session);
        folderAppService.assignOne(me, visitedCourseId, folderId);
        return "redirect:/MyPage";
    }

    /** 일괄: 폴더 배정 */
    @PostMapping("/saved/bulk-assign")
    public String bulkAssign(@RequestParam Long folderId,
                             @RequestParam(name = "itemIds") List<Long> visitedCourseIds,
                             HttpSession session) {
        Member me = requireLogin(session);
        folderAppService.assignBulk(me, visitedCourseIds, folderId);
        return "redirect:/MyPage";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileForm form, HttpSession session) {
        Member member = requireLogin(session);
        if (form.getDisplayName() != null) {
            member.setDisplayName(form.getDisplayName().trim());
        }
        member.setIntro(form.getIntro());
        member.setProfileImageUrl(form.getProfileImageUrl());
        memberRepository.save(member);
        return "redirect:/MyPage"; // PRG
    }

    @PostMapping("/visited")
    public String addVisited(@RequestParam Long courseId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                             LocalDate visitedAt,
                             @RequestParam(required = false) Integer rating,
                             @RequestParam(required = false) String note,
                             HttpSession session) {
        Member member = requireLogin(session);
        VisitedCourse vc = new VisitedCourse();
        vc.setMember(member);
        vc.setCourseId(courseId);
        vc.setVisitedAt(visitedAt != null ? visitedAt : LocalDate.now());
        vc.setRating(rating);
        vc.setNote(note);
        visitedCourseRepository.save(vc);
        return "redirect:/MyPage"; // PRG
    }

    @PostMapping("/visited/{id}/delete")
    public String deleteVisited(@PathVariable Long id, HttpSession session) {
        Member me = requireLogin(session);
        VisitedCourse vc = visitedCourseRepository.findById(id).orElseThrow();
        if (!vc.getMember().getId().equals(me.getId())) {
            throw new IllegalStateException("FORBIDDEN");
        }
        visitedCourseRepository.delete(vc);
        return "redirect:/MyPage"; // PRG
    }

    @ExceptionHandler(IllegalStateException.class)
    public String onIllegalState(IllegalStateException e) {
        if ("NEED_LOGIN".equals(e.getMessage())) return "redirect:/login";
        if ("FORBIDDEN".equals(e.getMessage())) return "redirect:/MyPage?error=forbidden";
        return "redirect:/MyPage?error=unknown";
    }

    /** 화면 바인딩용 DTO (템플릿의 savedItems와 필드명이 일치) */
    public record SavedItemView(
            Long id,
            String title,
            String description,
            String imageUrl,
            String badge,
            String meta,
            String link,
            LocalDateTime updatedAt,
            List<String> folders
    ) {}
}