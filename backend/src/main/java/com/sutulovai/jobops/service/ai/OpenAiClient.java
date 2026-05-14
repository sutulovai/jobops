package com.sutulovai.jobops.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sutulovai.jobops.config.OpenAiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final RestClient restClient;
    private final OpenAiProperties props;
    private final ObjectMapper mapper;

    public OpenAiClient(RestClient restClient, OpenAiProperties props, ObjectMapper mapper) {
        this.restClient = restClient;
        this.props = props;
        this.mapper = mapper;
    }

    public record CompletionResult(String content, String model, int promptTokens, int completionTokens) {}

    public CompletionResult complete(String systemPrompt, String userPrompt, String model, double temperature, int maxTokens) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            log.warn("⚠️ OpenAI API key not configured. Returning mock response.");
            return new CompletionResult("{\"recommendation\":\"MAYBE\",\"fitScore\":70,\"confidence\":50,\"summary\":\"OpenAI API key not configured.\",\"reasonsToApply\":[],\"reasonsToSkip\":[\"OpenAI not configured\"],\"hardBlockers\":[],\"redFlags\":[],\"uncertainties\":[\"All fields uncertain — OpenAI key missing\"],\"missingInfo\":[],\"roleFit\":70,\"stackFit\":70,\"domainFit\":70,\"seniorityFit\":70,\"locationFit\":70,\"languageFit\":70,\"companyTypeFit\":70,\"germanRequirement\":\"UNKNOWN\",\"relocationRisk\":\"UNCERTAIN\",\"salaryRisk\":\"UNCERTAIN\",\"freshnessRisk\":\"UNKNOWN\",\"suggestedPositioning\":\"Configure OpenAI API key\",\"suggestedOutreachAngle\":\"Configure OpenAI API key\",\"suggestedSalaryStrategy\":\"Configure OpenAI API key\",\"suggestedFirstMessage\":\"Configure OpenAI API key\",\"suggestedNextAction\":\"RESEARCH_MORE\",\"suggestedPriority\":50,\"stackKeywords\":[],\"domainKeywords\":[]}", model, 0, 0);
        }

        try {
            var body = mapper.createObjectNode();
            body.put("model", model);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            body.put("response_format", mapper.createObjectNode().put("type", "json_object"));

            var messages = body.putArray("messages");
            addMessage(messages, "system", systemPrompt);
            addMessage(messages, "user", userPrompt);

            var startMs = System.currentTimeMillis();
            var response = restClient.post()
                    .uri(props.baseUrl() + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + props.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            var elapsed = System.currentTimeMillis() - startMs;
            log.info("✅ OpenAI call completed in {}ms", elapsed);

            var json = mapper.readTree(response);
            var content = json.at("/choices/0/message/content").asText();
            var usedModel = json.at("/model").asText(model);
            var promptTok = json.at("/usage/prompt_tokens").asInt(0);
            var completionTok = json.at("/usage/completion_tokens").asInt(0);

            return new CompletionResult(content, usedModel, promptTok, completionTok);

        } catch (Exception e) {
            log.error("❌ OpenAI call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    private void addMessage(ArrayNode messages, String role, String content) {
        var msg = messages.addObject();
        msg.put("role", role);
        msg.put("content", content);
    }
}
