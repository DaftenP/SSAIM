package com.e203.document.service;

import java.util.List;

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
		ApiDocs apiDocs = ApiDocs.builder()
			.projectId(projectId)
			.content("")
			.build();
		return apiDocsRepository.save(apiDocs);
	}
}