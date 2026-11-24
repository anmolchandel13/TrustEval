package com.anmol.trusteval.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks which task was assigned to which evaluator.
 * The unique constraint on (evaluator_id, task_id) strictly guarantees that
 * the same user NEVER receives the same task twice.
 */
@Entity
@Table(
    name = "task_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"evaluator_id", "task_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Evaluator evaluator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private EvaluationTask task;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
}
