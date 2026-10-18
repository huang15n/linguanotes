package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassRequest(@NotBlank @Size(max = 180) String name,
                           String description) { }
