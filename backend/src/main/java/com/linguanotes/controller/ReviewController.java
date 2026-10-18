package com.linguanotes.controller;

import com.linguanotes.dto.*;
import com.linguanotes.security.AccountPrincipal;
import com.linguanotes.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/flashcards")
    public List<FlashcardResponse> cards(@AuthenticationPrincipal AccountPrincipal principal) {
        return reviews.listCards(principal.id());
    }

    @PostMapping("/flashcards")
    public ResponseEntity<FlashcardResponse> createCard(@AuthenticationPrincipal AccountPrincipal principal,
                                                        @Valid @RequestBody FlashcardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviews.createCard(principal.id(), request));
    }

    @PutMapping("/flashcards/{id}")
    public FlashcardResponse updateCard(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id,
                                        @Valid @RequestBody FlashcardRequest request) {
        return reviews.updateCard(principal.id(), id, request);
    }

    @DeleteMapping("/flashcards/{id}")
    public ResponseEntity<Void> deleteCard(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long id) {
        reviews.deleteCard(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/review/due")
    public List<ReviewCardResponse> due(@AuthenticationPrincipal AccountPrincipal principal) {
        return reviews.due(principal.id());
    }

    @PostMapping("/review/{cardId}/answer")
    public ReviewResult answer(@AuthenticationPrincipal AccountPrincipal principal, @PathVariable Long cardId,
                               @Valid @RequestBody ReviewAnswerRequest request) {
        return reviews.answer(principal.id(), cardId, request.rating());
    }

    @GetMapping("/progress/summary")
    public ProgressSummary summary(@AuthenticationPrincipal AccountPrincipal principal) {
        return reviews.summary(principal.id());
    }

    @GetMapping("/progress/history")
    public List<ProgressPoint> history(@AuthenticationPrincipal AccountPrincipal principal) {
        return reviews.history(principal.id());
    }
}