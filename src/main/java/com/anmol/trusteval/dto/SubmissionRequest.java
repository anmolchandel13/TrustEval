package com.anmol.trusteval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SubmissionRequest {
    @NotNull(message = "Evaluator ID is required")
    private Long evaluatorId;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Selected answer is required")
    @Pattern(regexp = "^(?i)(A|B)$", message = "Selected answer must be 'A' or 'B'")
    private String selectedAnswer;
}
