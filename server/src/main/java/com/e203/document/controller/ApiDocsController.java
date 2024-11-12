package com.e203.document.controller;

import static org.springframework.http.HttpStatus.*;

import com.e203.jwt.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.e203.document.collection.ApiDocs;
import com.e203.document.service.ApiDocsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApiDocsController {

    private final ApiDocsService apiDocsService;

    private final JWTUtil jwtUtil;

    @PostMapping("/api/v1/projects/{projectId}/api-docs")
    public ResponseEntity<String> createApiDocs(@PathVariable("projectId") String projectId) {
        ApiDocs result = apiDocsService.saveApiDocs(projectId);
        if (result == null) {
            return ResponseEntity.status(NOT_FOUND).body("API명세서 문서 생성 실패.");
        }
        return ResponseEntity.status(OK).body("API명세서 문서 생성 완료.");
    }

    @GetMapping("/api/v1/projects/{projectId}/api-docs")
    public ResponseEntity<String> findApiDocsContent(@PathVariable("projectId") String projectId) {
        String apiDocsContent = apiDocsService.getApiDocsContent(projectId);
        if (apiDocsContent == null) {
            ResponseEntity.status(NOT_FOUND).body(" API 명세 조회 실패");
        }
        return ResponseEntity.status(OK).body(apiDocsContent);
    }

    @GetMapping("/api/v1/projects/{projectId}/api-docs/generate")
    public ResponseEntity<String> generateFuncDescContent(@PathVariable int projectId,
                                                          @RequestParam("message") String message,
                                                          @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = apiDocsService.generateApiDocs(projectId, userId, message);
        if (result.equals("Not found")) {
            return ResponseEntity.status(NOT_FOUND).body(null);
        } else if (result.equals("Not authorized")) {
            return ResponseEntity.status(UNAUTHORIZED).body(null);
        } else {
            return ResponseEntity.status(OK).body(result);
        }
    }

}
