package com.e203.project.service;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.e203.project.dto.gitlabapi.GitlabMR;
import com.e203.project.dto.response.ProjectGitlabMRResponseDto;
import com.mongodb.assertions.Assertions;

@SpringBootTest
public class GitlabServiceTest {
	@Autowired
	private GitlabService gitlabService;

	@Test
	public void gitAllMR() {
		List<GitlabMR> allMR = gitlabService.getAllMR("2024-11-03T23:59:59Z", "2024-11-05T23:59:59Z", "821395",
			"ZAzUAjNW-iihg1iTXHRr");
		for(GitlabMR mr : allMR) {
			System.out.println("mr : "+mr);
		}


	}

	@Test
	public void findUserMR(){
		List<ProjectGitlabMRResponseDto> userMR = gitlabService.findUserMR("2024-11-03T23:59:59Z","2024-11-05T23:59:59Z", 1,
			1);
		userMR.forEach(System.out::println);
	}

	@Test
	public void testConnectWebhook(){
		boolean mapResponseEntity = gitlabService.connectWebhook("821395", "ZAzUAjNW-iihg1iTXHRr");
		Assertions.assertTrue(mapResponseEntity);
	}
}
