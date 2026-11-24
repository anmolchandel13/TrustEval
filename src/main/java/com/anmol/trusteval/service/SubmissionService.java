package com.anmol.trusteval.service;

import com.anmol.trusteval.dto.SubmissionRequest;
import com.anmol.trusteval.dto.SubmissionResponse;
import com.anmol.trusteval.entity.*;
import com.anmol.trusteval.exception.ApiException;
import com.anmol.trusteval.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final EvaluatorRepository evaluatorRepository;
    private final EvaluationTaskRepository taskRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final TrustScoreService trustScoreService;
    private final ConsensusService consensusService;

    @Transactional
    public SubmissionResponse submitEvaluation(SubmissionRequest request) {
        Evaluator evaluator = evaluatorRepository.findById(request.getEvaluatorId())
                .orElseThrow(() -> new ApiException("Evaluator not found", HttpStatus.NOT_FOUND));

        if (evaluator.getStatus() == EvaluatorStatus.FLAGGED_SPAMMER) {
            throw new ApiException("Evaluator is flagged as a spammer and cannot submit evaluations.", HttpStatus.FORBIDDEN);
        }

        EvaluationTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        if (submissionRepository.existsByEvaluatorIdAndTaskId(evaluator.getId(), task.getId())) {
            throw new ApiException("Duplicate submission blocked: Evaluator has already submitted this task.", HttpStatus.CONFLICT);
        }

        // Ensure assignment record exists and mark it completed
        TaskAssignment assignment = assignmentRepository
                .findByEvaluatorIdAndTaskId(evaluator.getId(), task.getId())
                .orElseGet(() -> TaskAssignment.builder()
                        .evaluator(evaluator)
                        .task(task)
                        .build());
        assignment.setCompleted(true);
        assignmentRepository.save(assignment);

        String normalizedAnswer = request.getSelectedAnswer().trim().toUpperCase();

        Submission submission = Submission.builder()
                .evaluator(evaluator)
                .task(task)
                .selectedAnswer(normalizedAnswer)
                .weightUsed(evaluator.getTrustScore())
                .build();
        submission = submissionRepository.save(submission);

        evaluator.setTotalEvaluations(evaluator.getTotalEvaluations() + 1);

        Boolean honeypotPassed = null;
        String message;

        if (Boolean.TRUE.equals(task.getIsHoneypot())) {
            honeypotPassed = trustScoreService.evaluateHoneypotAndUpdateTrust(evaluator, task, normalizedAnswer);
            message = honeypotPassed
                    ? "Honey-pot passed! Evaluator trust score increased."
                    : "Honey-pot failed! Evaluator trust score penalized.";
        } else {
            evaluatorRepository.save(evaluator);
            consensusService.evaluateConsensusIfReady(task);
            message = "Evaluation submitted and weighted consensus updated.";
        }

        return SubmissionResponse.builder()
                .submissionId(submission.getId())
                .evaluatorId(evaluator.getId())
                .taskId(task.getId())
                .selectedAnswer(normalizedAnswer)
                .wasHoneypot(task.getIsHoneypot())
                .honeypotPassed(honeypotPassed)
                .updatedTrustScore(evaluator.getTrustScore())
                .evaluatorStatus(evaluator.getStatus())
                .taskStatus(task.getStatus())
                .taskFinalAnswer(task.getFinalAnswer())
                .consensusConfidence(task.getConsensusConfidence())
                .message(message)
                .build();
    }
}
