package com.e203.project.dto.response;

import java.util.Base64;

import com.e203.project.entity.Project;
import com.e203.project.entity.ProjectMember;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JiraInfo {
	private String jiraApi;
	private String jiraProjectId;
	private String userEmail;
	private String encodedCredentials;
	private String jiraProjectBoardId;

	@Builder
	private JiraInfo(String jiraApi, String jiraProjectId, String userEmail, String encodedCredentials, String jiraProjectBoardId) {
		this.jiraApi = jiraApi;
		this.jiraProjectId = jiraProjectId;
		this.userEmail = userEmail;
		this.encodedCredentials = encodedCredentials;
		this.jiraProjectBoardId = jiraProjectBoardId;
	}

	public static JiraInfo create(ProjectMember leader){
		String jiraApi = leader.getProject().getJiraApi();
		String jiraProjectId = leader.getProject().getJiraProjectId();
		String userEmail = leader.getUser().getUserEmail();
		String encodedCredentials = Base64.getEncoder().encodeToString((userEmail + ":" + jiraApi).getBytes());
		String jiraProjectBoardId = leader.getProject().getJiraBoardId();

		return new JiraInfo(jiraApi, jiraProjectId, userEmail, encodedCredentials, jiraProjectBoardId);
	}
}
