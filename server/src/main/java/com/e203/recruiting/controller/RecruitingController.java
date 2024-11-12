package com.e203.recruiting.controller;

import com.e203.global.response.PaginationResponseDto;
import com.e203.jwt.JWTUtil;
import com.e203.recruiting.request.RecruitingApplicantEditRequestDto;
import com.e203.recruiting.request.RecruitingApplyRequestDto;
import com.e203.recruiting.request.RecruitingEditRequestDto;
import com.e203.recruiting.request.RecruitingWriteRequestDto;
import com.e203.recruiting.response.RecruitingApplyResponseDto;
import com.e203.recruiting.response.RecruitingPostDetailResponseDto;
import com.e203.recruiting.response.RecruitingPostResponseDto;
import com.e203.recruiting.service.RecruitingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitingController {

    private final RecruitingService recruitingService;

    private final JWTUtil jwtUtil;

    @PostMapping("/api/v1/recruiting/posts")
    public ResponseEntity<String> writePost(@RequestBody RecruitingWriteRequestDto dto,
                                            @RequestHeader("Authorization") String auth) {
        if (dto.getAuthor() == null || !jwtUtil.isPermitted(dto.getAuthor(), auth)) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        recruitingService.createPost(dto);
        return ResponseEntity.status(200).body("모집 게시글 등록이 완료되었습니다.");
    }

    @GetMapping("/api/v1/recruiting/posts/{postId}")
    public ResponseEntity<RecruitingPostDetailResponseDto> getPost(@PathVariable(name = "postId") int postId,
                                                                   @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        RecruitingPostDetailResponseDto dto = recruitingService.getPost(postId, userId);

        if (dto == null) {
            return ResponseEntity.status(404).body(null);
        } else {
            return ResponseEntity.status(200).body(dto);
        }
    }

    @GetMapping("/api/v1/recruiting/posts")
    public ResponseEntity<PaginationResponseDto<RecruitingPostResponseDto>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer position,
            @RequestParam(required = false) Integer campus,
            @RequestParam(required = false) Integer domain,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "1") Integer pageNum) {

        return ResponseEntity.status(200).body(
                recruitingService.searchPosts(title, position, campus, domain, status, size, pageNum));
    }

    @PatchMapping("/api/v1/recruiting/posts/{postId}")
    public ResponseEntity<String> updatePost(@RequestBody RecruitingEditRequestDto dto,
                                             @PathVariable(name = "postId") int postId,
                                             @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = recruitingService.updatePost(postId, dto, userId);
        if (result.equals("Not Authorized")) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        } else if (result.equals("Not found")) {
            return ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
        } else {
            return ResponseEntity.status(200).body("게시글 수정이 완료되었습니다.");
        }
    }

    @DeleteMapping("/api/v1/recruiting/posts/{postId}")
    public ResponseEntity<String> removePost(@PathVariable(name = "postId") int postId,
                                             @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = recruitingService.removePost(postId, userId);
        if (result.equals("Not Authorized")) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        } else if (result.equals("Not found")) {
            return ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
        } else {
            return ResponseEntity.status(200).body("삭제되었습니다.");
        }
    }

    @PostMapping("/api/v1/recruiting/posts/{postId}/applicants")
    public ResponseEntity<String> applyTeam(@PathVariable(name = "postId") int postId,
                                            @RequestHeader("Authorization") String auth,
                                            @RequestBody RecruitingApplyRequestDto dto) {
        if (!jwtUtil.isPermitted(dto.getUserId(), auth)) {
            return ResponseEntity.status(403).body("권한이 없습니다.");
        }
        String result = recruitingService.crateRecruitingMember(postId, dto);
        if (result.equals("Duplicated")) {
            return ResponseEntity.status(403).body("중복 지원은 불가합니다.");
        } else if (result.equals("Not found")) {
            return ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
        } else {
            return ResponseEntity.status(200).body("신청이 완료되었습니다.");
        }
    }

    @PatchMapping("/api/v1/recruiting/posts/{postId}/applicants/{applicantId}")
    public ResponseEntity<String> editApplicant(@PathVariable(name = "postId") int postId,
                                                @PathVariable(name = "applicantId") int applicantId,
                                                @RequestBody RecruitingApplicantEditRequestDto dto,
                                                @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = recruitingService.updateApplicant(postId, applicantId, userId, dto);

        return switch (result) {
            case "Not found" -> ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
            case "Not authorized" -> ResponseEntity.status(403).body("권한이 없습니다.");
            case "Does not match" -> ResponseEntity.status(403).body("잘못된 요청입니다.");
            default -> ResponseEntity.status(200).body("수정이 완료되었습니다.");
        };
    }

    @DeleteMapping("/api/v1/recruiting/posts/{postId}/applicants/{applicantId}")
    public ResponseEntity<String> removeApplicant(@PathVariable(name = "postId") int postId,
                                                  @PathVariable(name = "applicantId") int applicantId,
                                                  @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = recruitingService.removeApplicant(postId, applicantId, userId);

        return switch (result) {
            case "Not found" -> ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
            case "Not authorized" -> ResponseEntity.status(403).body("권한이 없습니다.");
            case "Does not match" -> ResponseEntity.status(403).body("잘못된 요청입니다.");
            default -> ResponseEntity.status(200).body("삭제가 완료되었습니다.");
        };
    }

    @GetMapping("/api/v1/users/{userId}/applications")
    public ResponseEntity<List<RecruitingApplyResponseDto>> getApplications(@PathVariable(name = "userId") int userId,
                                                                            @RequestHeader("Authorization") String auth) {
        if (!jwtUtil.isPermitted(userId, auth)) {
            return ResponseEntity.status(403).body(null);
        }
        return ResponseEntity.status(200).body(recruitingService.searchApplication(userId));
    }
}
