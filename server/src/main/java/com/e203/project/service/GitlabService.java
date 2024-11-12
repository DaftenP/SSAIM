package com.e203.project.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.e203.project.dto.gitlabapi.GitlabMR;
import com.e203.project.dto.gitlabapi.GitlabWebhook;
import com.e203.project.dto.request.ProjectGitlabConnectDto;
import com.e203.project.dto.response.ProjectGitlabMRResponseDto;
import com.e203.project.entity.Project;
import com.e203.project.entity.ProjectMember;
import com.e203.project.repository.ProjectMemberRepository;
import com.e203.project.repository.ProjectRepository;
import com.e203.user.entity.User;
import com.e203.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitlabService {
	private static final Logger log = LoggerFactory.getLogger(GitlabService.class);
	private static final int MAXPAGE = 1000;

	private final ProjectRepository projectRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final RestClient restClient;
	private final UserRepository userRepository;

	@Transactional
	public boolean setGitlabApi(ProjectGitlabConnectDto gitlabDto, int projectId) {
		Project project = projectRepository.findById(projectId).orElse(null);

		if (!isExist(project)) {
			return false;
		}
		project.setGitlabApi(gitlabDto.getGitlabApi());
		project.setGitlabProjectId(gitlabDto.getGitlabProjectId());

		connectWebhook(gitlabDto.getGitlabProjectId() ,gitlabDto.getGitlabApi());

		return true;
	}


	public List<ProjectGitlabMRResponseDto> findUserMR(String startDate, String endDate, Integer projectId,
		int userId) {
		if (!isExist(projectId)) {
			return null;
		}
		ProjectMember leader = getProjectLeader(projectId);
		if (!isExist(leader)) {
			return null;
		}

		String gitlabApiKey = leader.getProject().getGitlabApi();
		String gitlabProjectId = leader.getProject().getGitlabProjectId();
		User user = userRepository.findByUserId(userId);
		String userEmail = user.getUserEmail();

		StringTokenizer st = new StringTokenizer(userEmail, "@");
		String username = st.nextToken();
		List<GitlabMR> allMR = getAllMR(startDate, endDate, gitlabProjectId, gitlabApiKey);

		if (!isExist(allMR)) {
			return null;
		}

		ArrayList<ProjectGitlabMRResponseDto> allMrList = new ArrayList<>();

		allMR.stream()
			.filter(mr -> mr.getMergedBy().getUsername() != null)
			.filter(mr -> isSameUser(mr.getMergedBy().getUsername(), username))
			.forEach(mr -> allMrList.add(
				ProjectGitlabMRResponseDto.builder().title(mr.getTitle()).mergeDate(mr.getMergedAt()).build()));

		return allMrList;
	}

	public boolean connectWebhook(String gitlabProjectId, String gitlabApiKey){
		String uri = "https://lab.ssafy.com/api/v4/projects/"+gitlabProjectId+"/hooks";

		ResponseEntity<Map> response = restClient.post()
			.uri(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.header("PRIVATE-TOKEN",gitlabApiKey)
			.body(GitlabWebhook.create())
			.retrieve()
			.toEntity(Map.class);

		return response.getStatusCode().is2xxSuccessful();
	}

	public List<GitlabMR> getAllMR(String startDate, String endDate, String gitlabProjectId, String gitlabApiToken) {

		int page = 1;
		ArrayList<GitlabMR> mrList = new ArrayList<>();
		while (page < MAXPAGE) {
			String url = "https://lab.ssafy.com/api/v4/projects/" + gitlabProjectId + "/merge_requests"
				+ "?state=merged&merged_before=" + startDate + "&merged_after=" + endDate + "&per_page=100&page="
				+ page;

			try {
				String responseBody = restClient.get()
					.uri(url)
					.header("PRIVATE-TOKEN", gitlabApiToken) // GitLab Access Token
					.retrieve()
					.body(String.class);

				if (!isExist(responseBody) || responseBody.isEmpty() || responseBody.equals("[]")) {
					return mrList; // 더 이상 데이터가 없으면 종료
				}

				List<GitlabMR> mrs = parseMergeRequests(responseBody);
				List<GitlabMR> filteredMR = getFilteredMrByPeriod(startDate, endDate, mrs);
				mrList.addAll(filteredMR);

				page++;

			} catch (RestClientResponseException e) {
				// API 호출 중 오류 발생 시 처리
				log.error("API 호출 중 오류 발생");
				return null;
			}
		}
		return mrList;
	}

	private boolean isSameUser(String gitlabUsername, String username) {
		return username.equals(gitlabUsername);
	}

	private boolean withinPeriod(String startDate, String endDate, String mergedAt) {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		ZonedDateTime start = ZonedDateTime.parse(startDate, formatter);
		ZonedDateTime end = ZonedDateTime.parse(endDate, formatter);
		ZonedDateTime now = ZonedDateTime.parse(mergedAt);
		return !now.isBefore(start) && !now.isAfter(end);
	}

	private static <T> boolean isExist(T t) {
		return t != null;
	}

	private List<GitlabMR> getFilteredMrByPeriod(String startDate, String endDate, List<GitlabMR> mrs) {
		List<GitlabMR> filteredMR = new ArrayList<>();
		for (GitlabMR mr : mrs) {
			if (!isExist(mr.getMergedAt())) {
				continue;
			}

			if (withinPeriod(startDate, endDate, mr.getMergedAt())) {
				filteredMR.add(mr);
			}
		}
		return filteredMR;
	}

	private ProjectMember getProjectLeader(int projectId) {
		List<ProjectMember> leader = projectMemberRepository.findByProjectIdAndRole(projectId, 1);
		if (leader.size() != 1) {
			return null;
		}
		return leader.get(0);
	}

	public List<GitlabMR> parseMergeRequests(String jsonResponse) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(jsonResponse, new TypeReference<List<GitlabMR>>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
