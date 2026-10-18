package com.linguanotes.dto;

import java.time.LocalDateTime;

public record ClassResponse(Long id,
                            String name,
                            String description,
                            String joinCode,
                            Long teacherId,
                            String teacherName,
                            long memberCount,
                            boolean owner,
                            LocalDateTime createdAt) { }
