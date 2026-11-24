package com.anmol.trusteval.controller;

import com.anmol.trusteval.dto.AssignedTaskResponse;
import com.anmol.trusteval.dto.CreateTaskRequest;
import com.anmol.trusteval.entity.EvaluationTask;
import com.anmol.trusteval.entity.TaskStatus;
import com.anmol.trusteval.repository.EvaluationTaskRepository;
import com.anmol.trusteval.service.TaskAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "2. Evaluation Tasks & Max-Heap Allocation", description = "Create tasks, route urgent tasks via Max-Heap, and inspect consensus status")
public class TaskController {

    private final EvaluationTaskRepository taskRepository;
    private final TaskAllocationService allocationService;

    @PostMapping
    @Operation(summary = "Create a new evaluation task (standard task or hidden Honey-Pot)")
    public ResponseEntity<EvaluationTask> createTask(@Valid @RequestBody CreateTaskRequest request) {
        EvaluationTask task = EvaluationTask.builder()
                .promptText(request.getPromptText())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .priority(request.getPriority())
                .requiredReviewers(request.getRequiredReviewers())
                .isHoneypot(Boolean.TRUE.equals(request.getIsHoneypot()))
                .expectedAnswer(request.getExpectedAnswer() != null ? request.getExpectedAnswer().toUpperCase() : null)
                .status(TaskStatus.OPEN)
                .build();

        return new ResponseEntity<>(taskRepository.save(task), HttpStatus.CREATED);
    }

    @PostMapping("/allocate/{evaluatorId}")
    @Operation(summary = "Assign the highest-priority unseen task to an evaluator using a Max-Heap (PriorityQueue)")
    public ResponseEntity<AssignedTaskResponse> allocateTask(@PathVariable Long evaluatorId) {
        return ResponseEntity.ok(allocationService.allocateNextTask(evaluatorId));
    }

    @GetMapping
    @Operation(summary = "List all tasks (optionally filter by status: OPEN, COMPLETED, NEEDS_MANUAL_REVIEW)")
    public ResponseEntity<List<EvaluationTask>> getTasks(@RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return ResponseEntity.ok(taskRepository.findByStatus(status));
        }
        return ResponseEntity.ok(taskRepository.findAll());
    }
}
