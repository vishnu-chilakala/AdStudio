package com.cts.creative.dto;

import com.cts.creative.entity.CreativeAsset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreativeAssetResponse {

    private Long assetId;

    private Long brandId;

    private Long campaignBriefId;

    private String assetName;

    private String filePath;

    private Integer fileSizeKB;

    private Integer version;

    private Long uploadedById;

    private Integer width;

    private Integer height;

    private CreativeAsset.AssetType assetType;

    private CreativeAsset.Status status;
}