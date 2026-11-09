package com.fiveoutoffive.moa_backend.integration.application.service;

import com.fiveoutoffive.moa_backend.integration.application.result.NotionDatabaseResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** 노션 공개 API 호출. 권한 만료(401)는 NotionAuthExpiredException 으로 구분한다. */
@Slf4j
@Component
public class NotionClient {

    private static final String API = "https://api.notion.com/v1";
    private static final String VERSION = "2022-06-28";

    private final RestClient restClient = RestClient.create();

    public tools.jackson.databind.JsonNode exchangeCode(String code, String clientId, String clientSecret, String redirectUri) {
        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        return restClient.post().uri(API + "/oauth/token").header(HttpHeaders.AUTHORIZATION, "Basic " + basic).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("grant_type", "authorization_code", "code", code, "redirect_uri", redirectUri)).retrieve().body(tools.jackson.databind.JsonNode.class);
    }

    public List<NotionDatabaseResponse> searchDatabases(String token) {
        tools.jackson.databind.JsonNode body = authed(token).post().uri(API + "/search").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("filter", Map.of("value", "database", "property", "object"))).retrieve().body(tools.jackson.databind.JsonNode.class);
        List<NotionDatabaseResponse> result = new ArrayList<>();
        body.path("results").forEach(node -> result.add(new NotionDatabaseResponse(node.path("id").asString(""), node.path("title").path(0).path("plain_text").asString(""))));
        return result;
    }

    public String createPage(String token, String databaseId, String title, String content) {
        return authed(token).post().uri(API + "/pages").contentType(MediaType.APPLICATION_JSON).body(Map.of(
                "parent", Map.of("database_id", databaseId),
                "properties", Map.of("title", Map.of("title", List.of(text(title)))),
                "children", List.of(paragraph(content)))).retrieve().body(tools.jackson.databind.JsonNode.class).path("id").asString("");
    }

    public void updatePage(String token, String pageId, String content) {
        authed(token).patch().uri(API + "/blocks/" + pageId + "/children").contentType(MediaType.APPLICATION_JSON).body(Map.of("children", List.of(paragraph(content)))).retrieve().toBodilessEntity();
    }

    private RestClient authed(String token) {
        return restClient.mutate().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token).defaultHeader("Notion-Version", VERSION).build();
    }

    private Map<String, Object> text(String content) {
        return Map.of("type", "text", "text", Map.of("content", content));
    }

    private Map<String, Object> paragraph(String content) {
        return Map.of("object", "block", "type", "paragraph", "paragraph", Map.of("rich_text", List.of(text(content.length() > 2000 ? content.substring(0, 2000) : content))));
    }
}
