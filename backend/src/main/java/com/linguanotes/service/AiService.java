package com.linguanotes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.linguanotes.dto.AiGenerationResponse;
import com.linguanotes.dto.AiRequest;
import com.linguanotes.model.AiGeneration;
import com.linguanotes.repository.AiGenerationRepository;
import com.linguanotes.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AiService {
    private static final int DAILY_LIMIT = 20;
    private final AiGenerationRepository generations;
    private final UserAccountRepository accounts;
    private final RestClient restClient;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    public AiService(AiGenerationRepository generations, UserAccountRepository accounts, RestClient.Builder builder) {
        this.generations = generations;
        this.accounts = accounts;
        this.restClient = builder.build();
    }

    public AiGenerationResponse explain(Long ownerId, AiRequest request) {
        String prompt = "Explique clairement cette notion de langue" + target(request) + level(request) +
                ". Donne une règle, les exceptions utiles et des exemples.\n\n" + request.content();
        return generate(ownerId, "EXPLAIN", prompt);
    }

    public AiGenerationResponse enrichNote(Long ownerId, AiRequest request) {
        String prompt = "Améliore cette note linguistique" + target(request) + level(request) +
                ". Ajoute des exemples utiles et conserve les informations correctes. Réponds en texte Markdown.\n\n" + request.content();
        return generate(ownerId, "ENRICH_NOTE", prompt);
    }

    public AiGenerationResponse generateExercise(Long ownerId, AiRequest request) {
        String prompt = "Crée cinq exercices linguistiques avec corrigé et explications" + target(request) + level(request) +
                ". Présente consignes, questions, réponses et explications clairement.\n\n" + request.content();
        return generate(ownerId, "GENERATE_EXERCISE", prompt);
    }

    private AiGenerationResponse generate(Long ownerId, String type, String prompt) {
        if (apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Le fournisseur IA n’est pas configuré côté serveur");
        }
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long used = generations.countByOwnerIdAndCreatedAtAfter(ownerId, startOfDay);
        if (used >= DAILY_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "La limite quotidienne de génération a été atteinte");
        }

        JsonNode response;
        try {
            String endpoint = baseUrl.replaceAll("/+$", "") + "/chat/completions";
            response = restClient.post().uri(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(Map.of("role", "user", "content", prompt)),
                            "temperature", 0.4,
                            "max_tokens", 1800))
                    .retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Le fournisseur IA n’a pas répondu correctement");
        }
        JsonNode choices = response == null ? null : response.path("choices");
        String result = choices != null && choices.isArray() && !choices.isEmpty()
                ? choices.get(0).path("message").path("content").asText("") : "";
        if (result.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La réponse du fournisseur IA est vide");

        AiGeneration generation = new AiGeneration();
        generation.setOwner(accounts.getReferenceById(ownerId));
        generation.setGenerationType(type);
        generation.setStatus("COMPLETE");
        generation.setPromptHash(hash(prompt));
        generation.setResultContent(result);
        generation.setInputTokens(response.path("usage").path("prompt_tokens").asInt(0));
        generation.setOutputTokens(response.path("usage").path("completion_tokens").asInt(0));
        generation = generations.save(generation);
        return new AiGenerationResponse(generation.getId(), type, generation.getStatus(), result,
                Math.max(0, DAILY_LIMIT - (int) used - 1));
    }

    private String target(AiRequest request) {
        return request.targetLanguage() == null || request.targetLanguage().isBlank()
                ? "" : " pour la langue " + request.targetLanguage().trim();
    }

    private String level(AiRequest request) {
        return request.level() == null || request.level().isBlank()
                ? "" : " au niveau " + request.level().trim();
    }

    private String hash(String prompt) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(prompt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}