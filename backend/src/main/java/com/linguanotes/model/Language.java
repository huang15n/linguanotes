package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "languages") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Language {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false, length = 10) private String code;
}
