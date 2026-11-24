package com.anmol.trusteval.service;

import com.anmol.trusteval.dto.AssignedTaskResponse;
import com.anmol.trusteval.entity.*;
import com.anmol.trusteval.exception.ApiException;
import com.anmol.trusteval.repository.EvaluationTaskRepository;
import com.anmol.trusteval.repository.EvaluatorRepository;
import com.anmol.trusteval.repository.TaskAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
@RequiredArgsConstructor
public class TaskAllocationService {

    private final EvaluatorRepository evaluatorRepository;
    private final EvaluationTaskRepository taskRepository;
    private final TaskAssignmentRepository assignmentRepository;

    /**
     * Core Algorithm 1: Max-Heap Task Routing + No-Duplicate Prevention.
     *
     * How it works in simple steps:
     * 1. Checks that the evaluator exists and is ACTIVE (not a flagged spammer).
     * 2. Fetches all OPEN tasks from the database and loads them into a Java PriorityQueue (Max-Heap).
     * 3. The Max-Heap orders tasks by highest 'priority' first (and older 'createdAt' on ties).
     * 4. Polls tasks from the top of the Max-Heap and checks if this evaluator has already been
     *    assigned that task. The first unseen task is assigned and saved.
     */
    @Transactional
    public AssignedTaskResponse allocateNextTask(Long evaluatorId) {
        Evaluator evaluator = evaluatorRepository.findById(evaluatorId)
                .orElseThrow(() -> new ApiException("Evaluator not found with ID: " + evaluatorId, HttpStatus.NOT_FOUND));

        if (evaluator.getStatus() == EvaluatorStatus.FLAGGED_SPAMMER) {
            throw new ApiException("Evaluator is flagged as a spammer and cannot receive new tasks.", HttpStatus.FORBIDDEN);
        }

        List<EvaluationTask> openTasks = taskRepository.findByStatus(TaskStatus.OPEN);
        if (openTasks.isEmpty()) {
            throw new ApiException("No open evaluation tasks available right now.", HttpStatus.NOT_FOUND);
        }

        // Max-Heap Comparator: Higher priority comes first; if priority is equal, earlier createdAt comes first
        Comparator<EvaluationTask> maxHeapComparator = Comparator
                .comparingInt(EvaluationTask::getPriority).reversed()
                .thenComparing(EvaluationTask::getCreatedAt);

        PriorityQueue<EvaluationTask> maxHeap = new PriorityQueue<>(maxHeapComparator);
        maxHeap.addAll(openTasks);

        EvaluationTask selectedTask = null;

        // Poll from Max-Heap until we find a task this evaluator has NEVER received before
        while (!maxHeap.isEmpty()) {
            EvaluationTask candidate = maxHeap.poll();
            boolean alreadyAssigned = assignmentRepository.existsByEvaluatorIdAndTaskId(evaluatorId, candidate.getId());
            if (!alreadyAssigned) {
                selectedTask = candidate;
                break;
            }
        }

        if (selectedTask == null) {
            throw new ApiException("No new unseen tasks available for evaluator ID: " + evaluatorId, HttpStatus.NOT_FOUND);
        }

        TaskAssignment assignment = TaskAssignment.builder()
                .evaluator(evaluator)
                .task(selectedTask)
                .completed(false)
                .build();

        assignment = assignmentRepository.save(assignment);

        // Return DTO without exposing isHoneypot or expectedAnswer
        return AssignedTaskResponse.builder()
                .assignmentId(assignment.getId())
                .taskId(selectedTask.getId())
                .evaluatorId(evaluator.getId())
                .promptText(selectedTask.getPromptText())
                .optionA(selectedTask.getOptionA())
                .optionB(selectedTask.getOptionB())
                .priority(selectedTask.getPriority())
                .build();
    }
}
