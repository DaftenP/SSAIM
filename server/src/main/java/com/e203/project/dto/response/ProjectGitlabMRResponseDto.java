package com.e203.project.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectGitlabMRResponseDto {
	private String title;
	private String mergeDate;

	@Builder
	public ProjectGitlabMRResponseDto(String title, String mergeDate) {
		this.title = title;
		this.mergeDate = mergeDate;
	}
}
