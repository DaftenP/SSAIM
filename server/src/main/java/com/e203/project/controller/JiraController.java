package com.e203.project.controller;

import static org.springframework.http.HttpStatus.*;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.e203.project.dto.request.JiraIssueRequestDto;
import com.e203.project.dto.request.JiraSprintCreateRequestDto;
import com.e203.project.dto.response.JiraIssueResponseDto;
import com.e203.project.dto.request.ProjectJiraConnectDto;
import com.e203.project.dto.response.ProjectJiraEpicResponseDto;
import com.e203.project.dto.response.SprintResponseDto;
import com.e203.project.service.JiraService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class JiraController {

	private final JiraService jiraService;

	@PatchMapping("/api/v1/projects/{projectId}/jira-api")
	public ResponseEntity<String> connectJiraApi(@PathVariable("projectId") int projectId,
		@RequestBody ProjectJiraConnectDto projectJiraConnectDto) {

		boolean result = jiraService.setJiraApi(projectJiraConnectDto, projectId);

		if (result) {
			return ResponseEntity.status(OK).body("jira api 연결 성공");
		}
		return ResponseEntity.status(BAD_REQUEST).body("jira api 연결 실패");
	}

	@GetMapping("/api/v1/projects/{projectId}/issue")
	public ResponseEntity<List<JiraIssueResponseDto>> findAllJiraIssue(@PathVariable("projectId") int projectId,
		@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
		List<JiraIssueResponseDto> allJiraIssues = jiraService.findAllJiraIssues(startDate, endDate, projectId);
		if (allJiraIssues == null) {
			return ResponseEntity.status(NOT_FOUND).body(null);
		}
		if (allJiraIssues.isEmpty()) {
			return ResponseEntity.status(OK).body(allJiraIssues);
		}
		return ResponseEntity.status(OK).body(allJiraIssues);
	}

	@GetMapping("/api/v1/projects/{projectId}/epics")
	public ResponseEntity<List<ProjectJiraEpicResponseDto>> findAllEpic(@PathVariable("projectId") int projectId) {
		List<ProjectJiraEpicResponseDto> epics = jiraService.findAllEpics(projectId);
		if (epics == null) {
			return ResponseEntity.status(NOT_FOUND).body(null);
		}
		return ResponseEntity.status(OK).body(epics);
	}

	@PostMapping("/api/v1/projects/{projectId}/issue")
	public ResponseEntity<String> createIssue(@PathVariable("projectId") int projectId,
		@RequestBody JiraIssueRequestDto dto) {
		ResponseEntity<Map> result = jiraService.createIssue(projectId, dto);
		if (result.getStatusCode() == CREATED) {
			return ResponseEntity.status(OK).body("이슈 생성에 성공했습니다.");
		}
		return ResponseEntity.status(NOT_FOUND).body("이슈 생성에 실패했습니다.");
	}

	@PutMapping("/api/v1/projects/{projectId}/issue")
	public ResponseEntity<String> modifyIssue(@PathVariable("projectId") int projectId,
		@RequestBody JiraIssueRequestDto dto){
		ResponseEntity<Map> result = jiraService.modifyIssue(projectId, dto);

		if(result.getStatusCode()==NO_CONTENT){
			return ResponseEntity.status(OK).body("이슈 수정에 성공했습니다.");
		}
		return ResponseEntity.status(NOT_FOUND).body("이슈 수정에 실패했습니다.");
	}

	@GetMapping("/api/v1/projects/{projectId}/sprint")
	public ResponseEntity<List<SprintResponseDto>> findAllSprints(@PathVariable("projectId") int projectId) {
		List<SprintResponseDto> sprints = jiraService.findAllSprints(projectId);
		if(sprints==null){
			ResponseEntity.status(NOT_FOUND).body(null);
		}
		return ResponseEntity.status(OK).body(sprints);
	}

	@PostMapping("/api/v1/projects/{projectId}/sprint")
	public ResponseEntity<String> createSprint(@PathVariable("projectId") int projectId, @RequestBody JiraSprintCreateRequestDto dto){
		String sprint = jiraService.createSprint(dto, projectId);

		if(sprint.equals("Not Found")){
			return ResponseEntity.status(NOT_FOUND).body("스프린트 생성을 실패했습니다.");
		}
		return ResponseEntity.status(OK).body(sprint);
	}
}
