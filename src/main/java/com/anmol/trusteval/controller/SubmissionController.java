package com.anmol.trusteval.controller;

import com.anmol.trusteval.dto.SubmissionRequest;
import com.anmol.trusteval.dto.SubmissionResponse;
import com.anmol.trusteval.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "3. Submissions & Weighted Consensus", description = "Submit task evaluations, trigger honey-pot checks, and compute weighted consensus")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Submit an evaluation ('A' or 'B') for a task")
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(submissionService.submitEvaluation(request));
    }
}
