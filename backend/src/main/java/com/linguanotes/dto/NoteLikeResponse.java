package com.linguanotes.dto;

public record NoteLikeResponse(Long noteId, long likeCount, boolean likedByMe) { }
