package com.anmol.trusteval.service;

import com.anmol.trusteval.entity.*;
import com.anmol.trusteval.repository.EvaluationTaskRepository;
import com.anmol.trusteval.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsensusService {

    /**
     * Minimum weighted confidence (70%) needed to automatically finalize a task.
     * If confidence is below 0.70, the task is flagged as NEEDS_MANUAL_REVIEW.
     */
    public static final double CONFIDENCE_THRESHOLD = 0.70;

    private final SubmissionRepository submissionRepository;
    private final EvaluationTaskRepository taskRepository;

    /**
     * Core Algorithm 3: Weighted Consensus Engine.
     *
     * Instead of a naive 1-person-1-vote majority, each reviewer's vote is weighted by their trustScore.
     * - Spammers (FLAGGED_SPAMMER) are excluded from consensus weight.
     * - Confidence = WinningOptionWeight / (WeightA + WeightB)
     * - If Confidence >= 0.70 -> Task is marked COMPLETED with the winning finalAnswer.
     * - If Confidence < 0.70  -> Task is marked NEEDS_MANUAL_REVIEW for human inspection.
     */
    public void evaluateConsensusIfReady(EvaluationTask task) {
        if (Boolean.TRUE.equals(task.getIsHoneypot())) {
            return; // Honey-pot tasks already have a known ground-truth expectedAnswer
        }

        List<Submission> submissions = submissionRepository.findByTaskId(task.getId());
        if (submissions.size() < task.getRequiredReviewers()) {
            return; // Wait until enough reviewers have submitted
        }

        double weightA = 0.0;
        double weightB = 0.0;

        for (Submission sub : submissions) {
            Evaluator reviewer = sub.getEvaluator();
            // Ignore flagged spammers when computing final consensus
            if (reviewer.getStatus() == EvaluatorStatus.FLAGGED_SPAMMER) {
                continue;
            }
            double weight = sub.getWeightUsed() != null ? sub.getWeightUsed() : reviewer.getTrustScore();
            if ("A".equalsIgnoreCase(sub.getSelectedAnswer())) {
                weightA += weight;
            } else if ("B".equalsIgnoreCase(sub.getSelectedAnswer())) {
                weightB += weight;
            }
        }

        double totalWeight = weightA + weightB;
        if (totalWeight <= 0.0) {
            task.setStatus(TaskStatus.NEEDS_MANUAL_REVIEW);
            task.setConsensusConfidence(0.0);
            taskRepository.save(task);
            return;
        }

        String winningAnswer = weightA >= weightB ? "A" : "B";
        double winningWeight = Math.max(weightA, weightB);
        double confidence = Math.round((winningWeight / totalWeight) * 100.0) / 100.0;

        task.setConsensusConfidence(confidence);

        if (confidence >= CONFIDENCE_THRESHOLD && weightA != weightB) {
            task.setFinalAnswer(winningAnswer);
            task.setStatus(TaskStatus.COMPLETED);
        } else {
            task.setStatus(TaskStatus.NEEDS_MANUAL_REVIEW);
        }

        taskRepository.save(task);
    }
}
