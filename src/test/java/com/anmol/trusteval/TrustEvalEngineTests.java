package com.anmol.trusteval;

import com.anmol.trusteval.dto.AssignedTaskResponse;
import com.anmol.trusteval.dto.SubmissionRequest;
import com.anmol.trusteval.dto.SubmissionResponse;
import com.anmol.trusteval.entity.*;
import com.anmol.trusteval.exception.ApiException;
import com.anmol.trusteval.repository.*;
import com.anmol.trusteval.service.SubmissionService;
import com.anmol.trusteval.service.TaskAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TrustEvalEngineTests {

    @Autowired
    private EvaluatorRepository evaluatorRepository;

    @Autowired
    private EvaluationTaskRepository taskRepository;

    @Autowired
    private TaskAssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TaskAllocationService allocationService;

    @Autowired
    private SubmissionService submissionService;

    @BeforeEach
    void cleanDatabase() {
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        taskRepository.deleteAll();
        evaluatorRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Max-Heap Priority Queue assigns highest priority task first and never assigns duplicate task to same user")
    void testMaxHeapOrderingAndNoDuplicateAssignment() {
        Evaluator evaluator = evaluatorRepository.save(Evaluator.builder()
                .name("Test Reviewer")
                .email("reviewer@test.com")
                .trustScore(1.0)
                .build());

        EvaluationTask lowPriorityTask = taskRepository.save(EvaluationTask.builder()
                .promptText("Low priority prompt")
                .optionA("A")
                .optionB("B")
                .priority(20)
                .requiredReviewers(3)
                .isHoneypot(false)
                .build());

        EvaluationTask highPriorityTask = taskRepository.save(EvaluationTask.builder()
                .promptText("Urgent high priority prompt")
                .optionA("A")
                .optionB("B")
                .priority(90)
                .requiredReviewers(3)
                .isHoneypot(false)
                .build());

        // First allocation MUST pick priority 90 from the Max-Heap
        AssignedTaskResponse firstAssigned = allocationService.allocateNextTask(evaluator.getId());
        assertEquals(highPriorityTask.getId(), firstAssigned.getTaskId());

        // Second allocation for the SAME user MUST skip the already-assigned priority 90 task and pick priority 20
        AssignedTaskResponse secondAssigned = allocationService.allocateNextTask(evaluator.getId());
        assertEquals(lowPriorityTask.getId(), secondAssigned.getTaskId());

        // Third allocation must throw 404 because no unseen tasks remain for this user
        assertThrows(ApiException.class, () -> allocationService.allocateNextTask(evaluator.getId()));
    }

    @Test
    @DisplayName("2. Honey-Pot task penalizes wrong answers and flags evaluator as FLAGGED_SPAMMER when trust falls below 0.40")
    void testHoneypotPenaltyAndSpammerFlagging() {
        Evaluator spammer = evaluatorRepository.save(Evaluator.builder()
                .name("Spam User")
                .email("spam@test.com")
                .trustScore(0.50) // Close to threshold (0.40)
                .build());

        EvaluationTask honeypot = taskRepository.save(EvaluationTask.builder()
                .promptText("Hidden Honey-Pot Question")
                .optionA("Correct Option")
                .optionB("Wrong Option")
                .priority(80)
                .isHoneypot(true)
                .expectedAnswer("A")
                .build());

        SubmissionRequest wrongSub = new SubmissionRequest();
        wrongSub.setEvaluatorId(spammer.getId());
        wrongSub.setTaskId(honeypot.getId());
        wrongSub.setSelectedAnswer("B"); // Wrong answer on honey-pot!

        SubmissionResponse response = submissionService.submitEvaluation(wrongSub);

        assertTrue(response.getWasHoneypot());
        assertFalse(response.getHoneypotPassed());
        assertEquals(0.15, response.getUpdatedTrustScore(), 0.01); // 0.50 - 0.35 = 0.15
        assertEquals(EvaluatorStatus.FLAGGED_SPAMMER, response.getEvaluatorStatus());
    }

    @Test
    @DisplayName("3. Weighted Consensus gives higher weight to trusted reviewers and finalizes task when confidence >= 70%")
    void testWeightedConsensusResolution() {
        // 1 Senior high-trust reviewer (trust = 1.80) vs 1 low-trust reviewer (trust = 0.45)
        Evaluator seniorReviewer = evaluatorRepository.save(Evaluator.builder()
                .name("Senior Trusted")
                .email("senior@test.com")
                .trustScore(1.80)
                .build());

        Evaluator juniorReviewer = evaluatorRepository.save(Evaluator.builder()
                .name("Low Trust")
                .email("junior@test.com")
                .trustScore(0.45)
                .build());

        EvaluationTask task = taskRepository.save(EvaluationTask.builder()
                .promptText("Evaluate Java concurrency answer")
                .optionA("Option A")
                .optionB("Option B")
                .priority(75)
                .requiredReviewers(2)
                .isHoneypot(false)
                .build());

        // Senior votes "A" (weight 1.80)
        SubmissionRequest sub1 = new SubmissionRequest();
        sub1.setEvaluatorId(seniorReviewer.getId());
        sub1.setTaskId(task.getId());
        sub1.setSelectedAnswer("A");
        submissionService.submitEvaluation(sub1);

        // Junior votes "B" (weight 0.45)
        SubmissionRequest sub2 = new SubmissionRequest();
        sub2.setEvaluatorId(juniorReviewer.getId());
        sub2.setTaskId(task.getId());
        sub2.setSelectedAnswer("B");
        SubmissionResponse finalResponse = submissionService.submitEvaluation(sub2);

        // Total weight = 1.80 + 0.45 = 2.25. Option A confidence = 1.80 / 2.25 = 0.80 (80% >= 70%)
        assertEquals(TaskStatus.COMPLETED, finalResponse.getTaskStatus());
        assertEquals("A", finalResponse.getTaskFinalAnswer());
        assertEquals(0.80, finalResponse.getConsensusConfidence(), 0.01);
    }
}
