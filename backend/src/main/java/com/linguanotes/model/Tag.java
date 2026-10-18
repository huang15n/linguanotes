package com.linguanotes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tags") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
}
