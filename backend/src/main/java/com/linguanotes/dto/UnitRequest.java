package com.linguanotes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnitRequest(@NotBlank @Size(max = 180) String title, int position, String description) { }
