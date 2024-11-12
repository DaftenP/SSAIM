package com.e203.document.controller;

import static org.springframework.http.HttpStatus.*;

import com.e203.jwt.JWTUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.e203.document.collection.Proposal;
import com.e203.document.service.ProposalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    private final JWTUtil jwtUtil;

    @PostMapping("/api/v1/projects/{projectId}/proposal")
    public ResponseEntity<String> createProposal(@PathVariable String projectId) {
        Proposal result = proposalService.saveProposal(projectId);
        if (result == null) {
            return ResponseEntity.status(NOT_FOUND).body("기획서 생성 실패");
        }
        return ResponseEntity.status(OK).body("기획서 생성 성공"); // 201 Created 응답
    }

    @GetMapping("/api/v1/projects/{projectId}/proposal")
    public ResponseEntity<String> findProposalContent(@PathVariable String projectId) {
        String proposalContent = proposalService.getProposalContent(projectId);

        if (proposalContent == null) {
            return ResponseEntity.status(NOT_FOUND).body("기획서 조회 실패");
        }
        return ResponseEntity.status(OK).body(proposalContent);
    }

    @GetMapping("/api/v1/projects/{projectId}/proposal/generate")
    public ResponseEntity<String> generateApiDocsContent(@PathVariable("projectId") int projectId,
                                                         @RequestParam("message") String message,
                                                         @RequestHeader("Authorization") String auth) {
        int userId = jwtUtil.getUserId(auth.substring(7));
        String result = proposalService.generateProposal(projectId, userId, message);
        if (result.equals("Not found")) {
            return ResponseEntity.status(NOT_FOUND).body(null);
        } else if (result.equals("Not authorized")) {
            return ResponseEntity.status(UNAUTHORIZED).body(null);
        } else {
            return ResponseEntity.status(OK).body(result);
        }
    }

}
