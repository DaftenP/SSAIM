package com.e203.project.dto.request;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class JiraSprintCreateRequestDto {
	@Setter
	@JsonProperty("originBoardId")
	private String boardId;
	private String name;
	private String startDate;
	private String endDate;
	private String goal ;

	@Builder
	private JiraSprintCreateRequestDto(String name, String goal, String startDate, String endDate) {
		this.name = name;
		this.goal = goal;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
