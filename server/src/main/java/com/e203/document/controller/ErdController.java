package com.e203.document.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.e203.document.service.ErdService;
import com.e203.jwt.JWTUtil;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
public class ErdController {

	private final JWTUtil jwtUtil;
	private final ErdService erdService;

	@PostMapping("/api/v1/projects/{projectId}/ERD")
	public ResponseEntity<String> createErd(@PathVariable Integer projectId,
		@RequestPart(name = "ErdImage", required = false) MultipartFile image,
		@RequestHeader("Authorization") String auth) {

		int userId = jwtUtil.getUserId(auth.substring(7));
		String erd = erdService.createErd(projectId, userId, image);

		if ("success".equals(erd)) {
			return ResponseEntity.status(OK).body("ERD가 성공적으로 저장되었습니다.");
		} else if ("Not authorized".equals(erd)) {
			return ResponseEntity.status(UNAUTHORIZED).body(erd);
		} else if ("Not found".equals(erd)) {
			return ResponseEntity.status(NOT_FOUND).body(erd);
		} else {
			return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(erd);
		}
	}

	@PatchMapping("/api/v1/projects/{projectId}/ERD")
	public ResponseEntity<String> updateErd(@PathVariable Integer projectId,
		@RequestPart(name = "ErdImage", required = false) MultipartFile image,
		@RequestHeader("Authorization") String auth) {
		int userId = jwtUtil.getUserId(auth.substring(7));
		String erd = erdService.updateErd(projectId, userId, image);

		if ("success".equals(erd)) {
			return ResponseEntity.status(OK).body("ERD가 성공적으로 수정되었습니다.");
		} else if ("Not authorized".equals(erd)) {
			return ResponseEntity.status(UNAUTHORIZED).body(erd);
		} else if ("Not found".equals(erd)) {
			return ResponseEntity.status(NOT_FOUND).body(erd);
		} else {
			return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(erd);
		}
	}

	@GetMapping("/api/v1/projects/{projectId}/ERD")
	public ResponseEntity<String> findErd(@PathVariable Integer projectId,
		@RequestHeader("Authorization") String auth) {
		int userId = jwtUtil.getUserId(auth.substring(7));
		String erd = erdService.findErd(projectId, userId);
		if("Not authorized".equals(erd)) {
			return ResponseEntity.status(UNAUTHORIZED).body(erd);
		}else if("Not found".equals(erd)) {
			return ResponseEntity.status(NOT_FOUND).body(erd);
		}else{
			return ResponseEntity.status(OK).body(erd);
		}
	}
}

