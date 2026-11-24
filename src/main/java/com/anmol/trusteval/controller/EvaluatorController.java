package com.anmol.trusteval.controller;

import com.anmol.trusteval.dto.CreateEvaluatorRequest;
import com.anmol.trusteval.entity.Evaluator;
import com.anmol.trusteval.exception.ApiException;
import com.anmol.trusteval.repository.EvaluatorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluators")
@RequiredArgsConstructor
@Tag(name = "1. Evaluators", description = "Manage human reviewers and inspect their dynamic trust scores")
public class EvaluatorController {

    private final EvaluatorRepository evaluatorRepository;

    @PostMapping
    @Operation(summary = "Register a new evaluator with default trustScore = 1.0")
    public ResponseEntity<Evaluator> createEvaluator(@Valid @RequestBody CreateEvaluatorRequest request) {
        evaluatorRepository.findByEmail(request.getEmail()).ifPresent(e -> {
            throw new ApiException("Evaluator with this email already exists", HttpStatus.CONFLICT);
        });

        Evaluator evaluator = Evaluator.builder()
                .name(request.getName())
                .email(request.getEmail())
                .trustScore(1.0)
                .build();

        return new ResponseEntity<>(evaluatorRepository.save(evaluator), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all evaluators and their current trust scores / spammer status")
    public ResponseEntity<List<Evaluator>> getAllEvaluators() {
        return ResponseEntity.ok(evaluatorRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get evaluator details by ID")
    public ResponseEntity<Evaluator> getEvaluatorById(@PathVariable Long id) {
        Evaluator evaluator = evaluatorRepository.findById(id)
                .orElseThrow(() -> new ApiException("Evaluator not found", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(evaluator);
    }
}
