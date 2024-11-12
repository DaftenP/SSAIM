package com.e203.project.dto.jiraapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.e203.project.dto.request.JiraIssueRequestDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JiraIssueFields {
	JiraIssueRequest fields;

	@Builder
	private JiraIssueFields(JiraIssueRequest fields) {
		this.fields= fields;
	}

	public static JiraIssueFields transferJsonObject(JiraIssueRequestDto dto, String jiraProjectId, String jiraAccountId) {
		Map<String, String> content = new HashMap<String, String>();
		content.put("text", dto.getDescription());
		content.put("type", "text");
		IssueContent issueContent = IssueContent.builder()
			.content(List.of(content))
			.build();

		IssueDescription issueDescription = IssueDescription.builder()
			.content(List.of(issueContent))
			.build();

		JiraIssueRequest jiraIssueRequest = new JiraIssueRequest();
		jiraIssueRequest.setProject(new HashMap<>(){{
			put("key", jiraProjectId);
		}});
		jiraIssueRequest.setSummary(dto.getSummary());
		jiraIssueRequest.setEpicKey(dto.getEpicKey());
		jiraIssueRequest.setStoryPoint(dto.getStoryPoint());
		jiraIssueRequest.setDescription(issueDescription);
		jiraIssueRequest.setIssueType(new HashMap<>() {{
			put("name", dto.getIssueType());
		}});
		if(!jiraAccountId.isEmpty()){
			jiraIssueRequest.setAssignee(new HashMap<>() {{
				put("id",jiraAccountId);
			}});
		}


		return JiraIssueFields.builder()
			.fields(jiraIssueRequest)
			.build();
	}

}
