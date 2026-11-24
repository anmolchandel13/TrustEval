package com.anmol.trusteval.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String promptText;

    @Column(nullable = false, length = 500)
    private String optionA;

    @Column(nullable = false, length = 500)
    private String optionB;

    /**
     * Priority from 1 to 100.
     * Used by the Java PriorityQueue (Max-Heap) so higher priority tasks are assigned first.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 50;

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredReviewers = 3;

    /**
     * If true, this is a hidden test question (Honey-Pot) used to catch spammers.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isHoneypot = false;

    /**
     * Known ground-truth answer ("A" or "B") for Honey-Pot tasks. Null for standard tasks.
     */
    private String expectedAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    private String finalAnswer;

    private Double consensusConfidence;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
