package com.cts.creative.dto;

import java.time.LocalDateTime;

import com.cts.creative.entity.CreativeAsset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionHistoryResponse {

    private Long assetId;

    private Integer version;

    private String filePath;

    private String assetName;

    private CreativeAsset.Status status;

    private LocalDateTime updatedAt;
}