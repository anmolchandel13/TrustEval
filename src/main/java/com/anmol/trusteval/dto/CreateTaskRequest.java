package com.anmol.trusteval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Prompt text is required")
    private String promptText;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotNull(message = "Priority is required")
    @Min(1)
    @Max(100)
    private Integer priority = 50;

    @Min(1)
    @Max(15)
    private Integer requiredReviewers = 3;

    private Boolean isHoneypot = false;

    private String expectedAnswer;
}
