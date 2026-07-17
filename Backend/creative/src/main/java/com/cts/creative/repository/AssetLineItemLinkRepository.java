package com.cts.creative.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.creative.entity.AssetLineItemLink;
import com.cts.creative.entity.CreativeAsset;

public interface AssetLineItemLinkRepository
        extends JpaRepository<AssetLineItemLink, Long> {

    boolean existsByAssetAndLineItemId(
            CreativeAsset asset,
            Long lineItemId);
}