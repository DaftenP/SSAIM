package com.e203.global.utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatAiService {

    private final ChatClient chatClient;

    public ChatAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateWeeklyRemind(String message) {

        return chatClient.prompt()
                .system("아래의 내용의 답변을 한글로 해주고 일기 형태로 요약해줘 1일차 2일차 이렇게 구분하지 말고 하나의 이야기처럼 요약해줘")
                .user(message)
                .call()
                .content();
    }

    public String generateMeetingSummary(String message) {

        return chatClient.prompt()
                .system("아래의 내용을 전체 대화의 주제, 나온 의견들, 미래 방향성의 3주제로 나누어서 요약해줘")
                .user(message)
                .call()
                .content();
    }

    public String generateProposal(String message) {

        return chatClient.prompt()
                .system("사용자의 요청을 바탕으로 소프트웨어 프로젝트 기획서를 작성해줘. 기획서 양식은 아래와 같이 한글로 자세하게 작성해줘.\n" +
                        "{\n" +
                        "\t  \"title\": \"[서비스 명]\",\n" +
                        "\t  \"description\": \"[서비스 소개]\", \n" +
                        "\t  \"background\": \"[기획 배경]\",\n" +
                        "\t  \"feature\":\"[주요 기능 소개]\",\n" +
                        "\t  \"effect\": \"[기대효과]\"" +
                        "}\n" +
                        "json 내용을 파싱하고 싶어. json만 응답해줘.")
                .user(message)
                .call()
                .content();
    }

    public String generateFunctionDescription(String message, String proposal) {
        return chatClient.prompt()
                .system("사용자의 요청과 아래의 프로젝트 기획서 내용을 바탕으로 소프트웨어 프로젝트의 기능 명세서를 작성해줘.\n" +
                        proposal +
                        "\n" +
                        "기능 명세서 양식은 아래와 같이 한글로 자세하게 작성해줘.\n" +
                        "{\n" +
                        "    \"category\": [\"분류1\", \"분류2\", ...],\n" +
                        "    \"functionName\": [\"기능1\", \"기능2\", ...],\n" +
                        "    \"description\": [\"설명1\", \"설명2\", ...],\n" +
                        "    \"priority\": [\"n순위\", \"m순위\", ...]\n" +
                        "}\n" +
                        "json 내용을 파싱하고 싶어. json만 응답해줘.")
                .user(message)
                .call()
                .content();
    }

    public String generateApiDocs(String message, String funcDesc) {
        return chatClient.prompt()
                .system("사용자의 요청과 아래의 프로젝트 기능 명세서 내용을 바탕으로 소프트웨어 프로젝트의 API 명세서를 작성해줘.\n" +
                        funcDesc +
                        "\n" +
                        "기능 명세서 양식은 아래와 같이 한글로 자세하게 작성해줘.\n" +
                        "{\n" +
                        "    \"category\": [\"분류1\", \"분류2\", ...],\n" +
                        "    \"functionName\": [\"기능1\", \"기능2\", ...],\n" +
                        "    \"description\": [\"설명1\", \"설명2\", ...],\n" +
                        "    \"uri\": [\"uri1\", \"uri2\", ...],\n" +
                        "    \"method\": [\"메소드1\", \"메소드2\", ...],\n" +
                        "    \"priority\": [\"n순위\", \"m순위\", ...],\n" +
                        "}\n" +
                        "json 내용을 파싱하고 싶어. json만 응답해줘.")
                .user(message)
                .call()
                .content();
    }
}
