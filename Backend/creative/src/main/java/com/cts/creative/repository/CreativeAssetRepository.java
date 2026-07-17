package com.cts.creative.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.creative.entity.CreativeAsset;

public interface CreativeAssetRepository
        extends JpaRepository<CreativeAsset, Long> {
}