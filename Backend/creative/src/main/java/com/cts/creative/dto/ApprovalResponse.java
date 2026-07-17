package com.cts.creative.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    private Long approvalId;

    private Long assetId;

    private Long reviewerId;

    private LocalDate reviewDate;

    private String decision;

    private String feedback;

    private String status;
}