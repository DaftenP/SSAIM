package com.e203.project.controller;

import static org.springframework.http.HttpStatus.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.e203.jwt.JWTUtil;
import com.e203.project.dto.request.ProjectGitlabConnectDto;
import com.e203.project.dto.response.ProjectGitlabMRResponseDto;
import com.e203.project.service.GitlabService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GitlabController {
	private final JWTUtil jwtUtil;
	private final GitlabService gitlabService;


	@PatchMapping("api/v1/projects/{projectId}/gitlab-api")
	public ResponseEntity<String> connectGitlabApi(@PathVariable("projectId") int projectId,
		@RequestBody ProjectGitlabConnectDto projectGitlabConnectDto) {

		boolean result = gitlabService.setGitlabApi(projectGitlabConnectDto, projectId);

		if (result) {
			return ResponseEntity.status(OK).body("gitlab key 등록에 성공했습니다.");
		} else {
			return ResponseEntity.status(NOT_FOUND).body("gitlab key 등록에 실패했습니다.");
		}
	}

	@GetMapping("/api/v1/projects/{projectId}/gitlab-api")
	public ResponseEntity<List<ProjectGitlabMRResponseDto>> findUserAllMR(@RequestParam String startDate, @RequestParam String endDate,
		@PathVariable int projectId,
		@RequestHeader("Authorization") String auth) {
		int userId = jwtUtil.getUserId(auth.substring(7));
		List<ProjectGitlabMRResponseDto> userMR = gitlabService.findUserMR(startDate, endDate, projectId, userId);
		if(userMR==null){
			return ResponseEntity.status(NOT_FOUND).body(null);
		}
		return ResponseEntity.status(OK).body(userMR);
	}
}
