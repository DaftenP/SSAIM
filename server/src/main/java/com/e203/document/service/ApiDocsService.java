package com.e203.document.service;

import java.util.List;
import java.util.Optional;

import com.e203.global.utils.ChatAiService;
import com.e203.project.entity.Project;
import com.e203.project.repository.ProjectRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.e203.document.collection.ApiDocs;
import com.e203.document.repository.ApiDocsRepository;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiDocsService {

	private final MongoTemplate mongoTemplate;

	private final ApiDocsRepository apiDocsRepository;

	private final ProjectRepository projectRepository;

	private final ChatAiService chatAiService;

	private final FunctionDescriptionService functionDescriptionService;

	public void createIndex() {
		// Collation 설정
		Collation collation = Collation.of("ko");

		// 인덱스 정의
		IndexDefinition indexDefinition = new Index()
			.on("createdAt", Sort.Direction.DESC)
			.collation(collation);

		// 인덱스 추가
		IndexOperations indexOps = mongoTemplate.indexOps(ApiDocs.class);
		indexOps.ensureIndex(indexDefinition);
	}

	public ApiDocs getApiDocs(String projectId) {
		List<ApiDocs> results = apiDocsRepository.findByProjectId(projectId);
		return results.isEmpty() ? null : results.get(0); // 결과가 없으면 null 반환
	}

	public String getApiDocsContent(String projectId){
		ApiDocs apiDocs = getApiDocs(projectId);
		return apiDocs.getContent();
	}

	public ApiDocs updateApiDocsContent(String projectId, String content) {

		Query query = new Query(Criteria.where("projectId").is(projectId));
		Update update = new Update().set("content", content);
		UpdateResult documents = mongoTemplate.updateFirst(query, update, "ApiDocs");

		return getApiDocs(projectId);
	}

	public ApiDocs saveApiDocs(String projectId) {
		String defaultForm = "{\"category\": [],\"description\": [],\"url\": [],\"method\": [],\"functionName\": [],\"frontOwner\": [],\"backOwner\": [],\"frontState\": [],\"backState\": [],\"priority\": [],\"requestHeader\": [],\"responseHeader\": [],\"requestBody\": [],\"responseBody\": [] } ";
		ApiDocs apiDocs = ApiDocs.builder()
			.projectId(projectId)
			.content(defaultForm)
			.build();
		return apiDocsRepository.save(apiDocs);
	}

	public String generateApiDocs(int projectId, int userId, String message) {
		Optional<Project> project = projectRepository.findById(projectId);
		if (project.isEmpty()) {
			return "Not found";
		} else if (project.get().getProjectMemberList().stream()
				.noneMatch(member -> member.getUser().getUserId() == userId)) {
			return "Not authorized";
		}
		return chatAiService.generateApiDocs(message, functionDescriptionService.getFuncDescContent(String.valueOf(projectId)));
	}

}
