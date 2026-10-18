package com.linguanotes.dto;

import java.math.BigDecimal;

public record ProgressPoint(java.time.LocalDate date, long attempts, BigDecimal averageScore) { }
