package com.cts.advertiser.service;

import java.util.List;

import com.cts.advertiser.dto.request.TargetAudienceRequest;
import com.cts.advertiser.dto.response.TargetAudienceResponse;

public interface TargetAudienceService {

    // Creates a new target audience recors under a campaign brief
    TargetAudienceResponse createTargetAudience(TargetAudienceRequest request);

    // Returns all target audience records in the system
    List<TargetAudienceResponse> getAllTargetAudience();

    // Returns all audience records belonging to a specific campaign brief
    List<TargetAudienceResponse> getAllAudienceByBriefId(Integer briefId);

    // Updates an existing target audience record by its ID
    TargetAudienceResponse updateTargetAudience(Integer id, TargetAudienceRequest request);

    // Deletes a target audience record by its ID
    void deleteTargetAudience(Integer id);
    
}
