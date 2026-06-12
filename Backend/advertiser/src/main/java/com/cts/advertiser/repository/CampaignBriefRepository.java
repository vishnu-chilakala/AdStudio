package com.cts.advertiser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.advertiser.entity.CampaignBrief;

@Repository
public interface CampaignBriefRepository extends JpaRepository<CampaignBrief, Integer> { }
