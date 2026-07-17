package com.cts.creative.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {

    @NotNull(message = "Reviewer Id is required")
    private Long reviewerId;

    @NotBlank(message = "Decision is required")
    private String decision;

    private String feedback;
}