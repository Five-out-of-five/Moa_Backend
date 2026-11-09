package com.fiveoutoffive.moa_backend.integration.application.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Incoming Webhook 호출. 실패는 예외 대신 false 로 돌려준다. */
@Slf4j
@Component
public class SlackSender {

    private final RestClient restClient = RestClient.create();

    public boolean send(String webhookUrl, String text) {
        try {
            restClient.post().uri(webhookUrl).contentType(MediaType.APPLICATION_JSON).body(Map.of("text", text)).retrieve().toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("[Slack] 발송 실패: {}", e.getMessage());
            return false;
        }
    }
}
