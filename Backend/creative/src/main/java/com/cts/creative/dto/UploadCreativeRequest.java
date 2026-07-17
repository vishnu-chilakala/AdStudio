package com.cts.creative.dto;

import com.cts.creative.entity.CreativeAsset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadCreativeRequest {

    @NotNull(message = "Brand Id is required")
    private Long brandId;

    @NotNull(message = "Campaign Brief Id is required")
    private Long campaignBriefId;

    @NotBlank(message = "Asset Name is required")
    private String assetName;

    @NotNull(message = "Uploaded By Id is required")
    private Long uploadedById;

    @NotNull(message = "Asset Type is required")
    private CreativeAsset.AssetType assetType;

    @NotNull(message = "Width is required")
    private Integer width;

    @NotNull(message = "Height is required")
    private Integer height;
}