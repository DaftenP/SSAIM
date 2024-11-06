package com.e203.recruiting.controller;

import com.e203.jwt.JWTUtil;
import com.e203.recruiting.request.RecruitingWriteRequestDto;
import com.e203.recruiting.response.RecruitingPostDetailResponseDto;
import com.e203.recruiting.response.RecruitingPostResponseDto;
import com.e203.recruiting.service.RecruitingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<List<RecruitingPostResponseDto>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer position,
            @RequestParam(required = false) Integer campus,
            @RequestParam(required = false) Integer domain,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page) {

        return ResponseEntity.status(200).body(recruitingService.searchPosts(title, position, campus, domain, status, page));
    }
}