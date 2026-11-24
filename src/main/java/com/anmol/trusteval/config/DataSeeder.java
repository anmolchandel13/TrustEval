package com.anmol.trusteval.config;

import com.anmol.trusteval.entity.EvaluationTask;
import com.anmol.trusteval.entity.Evaluator;
import com.anmol.trusteval.entity.EvaluatorStatus;
import com.anmol.trusteval.entity.TaskStatus;
import com.anmol.trusteval.repository.EvaluationTaskRepository;
import com.anmol.trusteval.repository.EvaluatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Automatically seeds sample evaluators, high-priority normal tasks, and hidden Honey-Pot tasks
 * on startup so you can test all endpoints immediately in Swagger UI.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EvaluatorRepository evaluatorRepository;
    private final EvaluationTaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (evaluatorRepository.count() == 0) {
            Evaluator e1 = Evaluator.builder()
                    .name("Anmol Chandel (Senior Reviewer)")
                    .email("anmolchandel13@gmail.com")
                    .trustScore(1.60)
                    .status(EvaluatorStatus.ACTIVE)
                    .build();

            Evaluator e2 = Evaluator.builder()
                    .name("Rohan Verma (Standard Reviewer)")
                    .email("rohan@example.com")
                    .trustScore(1.00)
                    .status(EvaluatorStatus.ACTIVE)
                    .build();

            Evaluator e3 = Evaluator.builder()
                    .name("Priya Singh (New Reviewer)")
                    .email("priya@example.com")
                    .trustScore(0.80)
                    .status(EvaluatorStatus.ACTIVE)
                    .build();

            evaluatorRepository.saveAll(List.of(e1, e2, e3));
        }

        if (taskRepository.count() == 0) {
            // 1. Urgent Honey-Pot Task (Priority 95 - Will be popped first from Max-Heap!)
            EvaluationTask hp1 = EvaluationTask.builder()
                    .promptText("[Golden Check] What is the time complexity of binary search on a sorted array of size N?")
                    .optionA("O(log N)")
                    .optionB("O(N^2)")
                    .priority(95)
                    .requiredReviewers(3)
                    .isHoneypot(true)
                    .expectedAnswer("A")
                    .status(TaskStatus.OPEN)
                    .build();

            // 2. High-Priority Normal RLHF Task (Priority 85)
            EvaluationTask t1 = EvaluationTask.builder()
                    .promptText("Compare the two Java Spring Boot responses explaining @Transactional rollback behavior.")
                    .optionA("Response A: Explains that unchecked RuntimeExceptions trigger rollback by default.")
                    .optionB("Response B: Claims @Transactional rolls back on all checked exceptions automatically.")
                    .priority(85)
                    .requiredReviewers(3)
                    .isHoneypot(false)
                    .status(TaskStatus.OPEN)
                    .build();

            // 3. Medium-Priority Normal RLHF Task (Priority 60)
            EvaluationTask t2 = EvaluationTask.builder()
                    .promptText("Which SQL query optimization advice is safer for high-concurrency MySQL tables?")
                    .optionA("Response A: Use composite B-Tree indexes matching the WHERE and ORDER BY clauses.")
                    .optionB("Response B: Disable all foreign keys and run full table scans in production.")
                    .priority(60)
                    .requiredReviewers(3)
                    .isHoneypot(false)
                    .status(TaskStatus.OPEN)
                    .build();

            taskRepository.saveAll(List.of(hp1, t1, t2));
        }
    }
}
