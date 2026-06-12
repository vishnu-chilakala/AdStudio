package com.cts.advertiser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.advertiser.entity.TargetAudience;

@Repository
public interface TargetAudienceRepository extends JpaRepository<TargetAudience, Integer> { }