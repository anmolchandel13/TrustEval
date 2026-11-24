package com.anmol.trusteval.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO returned when a task is assigned to an evaluator.
 * NOTE: isHoneypot and expectedAnswer are intentionally hidden so the reviewer
 * cannot tell whether this is a normal task or a hidden Honey-Pot test question!
 */
@Data
@Builder
public class AssignedTaskResponse {
    private Long assignmentId;
    private Long taskId;
    private Long evaluatorId;
    private String promptText;
    private String optionA;
    private String optionB;
    private Integer priority;
}
