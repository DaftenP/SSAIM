package com.e203.project.dto.gitlabapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitlabWebhook {
	@JsonProperty("url")
	private String url = "https://k11e203.p.ssafy.io/api/v1/notification";
	@JsonProperty("merge_requests_events")
	private boolean merge_requests_events =true;
	@JsonProperty("token")
	private String token = "lucky3dragons";

	private GitlabWebhook(){
	}

	public static GitlabWebhook create(){
		return new GitlabWebhook();
	}
}
