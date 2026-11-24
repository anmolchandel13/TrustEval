package com.anmol.trusteval.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Trust score ranges from 0.0 to 2.0 (Starts at 1.0).
     * Higher trustScore gives higher weight in Weighted Consensus.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double trustScore = 1.0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalEvaluations = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer honeypotsAttempted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer honeypotsPassed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EvaluatorStatus status = EvaluatorStatus.ACTIVE;
}
