package com.anmol.trusteval.dto;

import com.anmol.trusteval.entity.EvaluatorStatus;
import com.anmol.trusteval.entity.TaskStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionResponse {
    private Long submissionId;
    private Long evaluatorId;
    private Long taskId;
    private String selectedAnswer;
    private Boolean wasHoneypot;
    private Boolean honeypotPassed;
    private Double updatedTrustScore;
    private EvaluatorStatus evaluatorStatus;
    private TaskStatus taskStatus;
    private String taskFinalAnswer;
    private Double consensusConfidence;
    private String message;
}
