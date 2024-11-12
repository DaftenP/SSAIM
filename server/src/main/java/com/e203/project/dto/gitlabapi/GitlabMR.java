package com.e203.project.dto.gitlabapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabMR {
	private int id;
	private int iid;
	@JsonProperty("project_id")
	private int projectId;
	private String title;
	private String description;
	private String state;
	@JsonProperty("created_at")
	private String createdAt;
	@JsonProperty("updated_at")
	private String updatedAt;
	@JsonProperty("merged_by")
	private GitlabUser mergedBy;// 머지한 사용자
	@JsonProperty("merged_at")
	private String mergedAt; // 머지된 시간
	@JsonProperty("target_branch")
	private String targetBranch; // 타겟 브랜치
	@JsonProperty("source_branch")
	private String sourceBranch; // 소스 브랜치
	private GitlabUser author; // 작성자
	private List<GitlabUser> assignees; // 담당자 리스트
	private GitlabUser assignee; // 담당자
	@JsonProperty("web_url")
	private String webUrl; // 머지 요청 URL
}
