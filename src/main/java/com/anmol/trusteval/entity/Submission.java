package com.anmol.trusteval.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"evaluator_id", "task_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Evaluator evaluator;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private EvaluationTask task;

    /**
     * Reviewer's chosen answer: "A" or "B".
     */
    @Column(nullable = false)
    private String selectedAnswer;

    /**
     * Snapshot of the evaluator's trustScore at the time of submission.
     */
    @Column(nullable = false)
    private Double weightUsed;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();
}
