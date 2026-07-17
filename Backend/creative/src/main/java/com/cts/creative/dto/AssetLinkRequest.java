package com.cts.creative.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetLinkRequest {

    @NotNull(message = "Asset Id is required")
    private Long assetId;

    @NotNull(message = "Line Item Id is required")
    private Long lineItemId;
}