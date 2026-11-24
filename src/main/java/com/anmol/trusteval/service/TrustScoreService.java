package com.anmol.trusteval.service;

import com.anmol.trusteval.entity.EvaluationTask;
import com.anmol.trusteval.entity.Evaluator;
import com.anmol.trusteval.entity.EvaluatorStatus;
import com.anmol.trusteval.repository.EvaluatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrustScoreService {

    public static final double REWARD_STEP = 0.10;
    public static final double PENALTY_STEP = 0.35;
    public static final double SPAMMER_THRESHOLD = 0.40;
    public static final double MAX_TRUST = 2.00;
    public static final double MIN_TRUST = 0.00;

    private final EvaluatorRepository evaluatorRepository;

    /**
     * Core Algorithm 2: Honey-Pot (Hidden Test Question) Spam Detection & Trust Update.
     *
     * - If the reviewer answers a Honey-Pot task CORRECTLY:
     *   trustScore increases by +0.10 (up to max 2.0).
     * - If the reviewer answers a Honey-Pot task INCORRECTLY:
     *   trustScore drops by -0.35 (down to min 0.0).
     * - If trustScore drops below 0.40, the evaluator is automatically marked as FLAGGED_SPAMMER.
     */
    public boolean evaluateHoneypotAndUpdateTrust(Evaluator evaluator, EvaluationTask honeypotTask, String selectedAnswer) {
        evaluator.setHoneypotsAttempted(evaluator.getHoneypotsAttempted() + 1);

        boolean passed = honeypotTask.getExpectedAnswer() != null
                && honeypotTask.getExpectedAnswer().equalsIgnoreCase(selectedAnswer.trim());

        double currentTrust = evaluator.getTrustScore();

        if (passed) {
            evaluator.setHoneypotsPassed(evaluator.getHoneypotsPassed() + 1);
            double updated = Math.min(MAX_TRUST, Math.round((currentTrust + REWARD_STEP) * 100.0) / 100.0);
            evaluator.setTrustScore(updated);
        } else {
            double updated = Math.max(MIN_TRUST, Math.round((currentTrust - PENALTY_STEP) * 100.0) / 100.0);
            evaluator.setTrustScore(updated);
            if (updated < SPAMMER_THRESHOLD) {
                evaluator.setStatus(EvaluatorStatus.FLAGGED_SPAMMER);
            }
        }

        evaluatorRepository.save(evaluator);
        return passed;
    }
}
