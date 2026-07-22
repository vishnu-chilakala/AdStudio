package com.cts.advertiser.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.advertiser.dto.request.CampaignBriefApprovalRequest;
import com.cts.advertiser.dto.request.CampaignBriefRequest;
import com.cts.advertiser.dto.response.CampaignBriefApprovalResponse;
import com.cts.advertiser.dto.response.CampaignBriefResponse;
import com.cts.advertiser.service.CampaignBriefApprovalService;
import com.cts.advertiser.service.CampaignBriefService;
import com.cts.advertiser.shared.ApiResponse;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; 

@RestController
@RequestMapping("/api/campaign-briefs")
@RequiredArgsConstructor
public class CampaignBriefController {

    // Injected automatically by Spring
    private final CampaignBriefService campaignBriefService;
    private final CampaignBriefApprovalService campaignBriefApprovalService;

    // POST /api/campaign-briefs - creates a new campaign brief

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> createCampaignBrief(@Valid @RequestBody CampaignBriefRequest request) {

        CampaignBriefResponse response = campaignBriefService.createCampaignBrief(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Campaign brief created successfully", response));

    }

    // GET /api/campaign-briefs - returns all campaign briefs

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignBriefResponse>>> getAllCampaignBriefs() {

        List<CampaignBriefResponse> response = campaignBriefService.getAllCampaignBriefs();

        return ResponseEntity.ok(ApiResponse.success("Campaign briefs retrieved successfully", response));

    }

    // GET /api/campaign-briefs/{id} - returns one campaign brief by ID

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> getCampaignBriefById(@PathVariable Integer id) {

        CampaignBriefResponse response = campaignBriefService.getCampaignBriefById(id);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief retrieved successfully", response));

    }

    // GET /api/campaign-briefs/brand/{brandId} - returns all campaign briefs for a specific brand

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<ApiResponse<List<CampaignBriefResponse>>> getAllBriefsByBrandId(@PathVariable Integer brandId) {

        List<CampaignBriefResponse> response = campaignBriefService.getAllBriefsByBrandId(brandId);

        return ResponseEntity.ok(ApiResponse.success("Campaign briefs retrieved successfully", response));

    }

    // PUT /api/campaign-briefs/{id} - updates an existing campaign brief

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> updateCampaignBrief(@PathVariable Integer id, @Valid @RequestBody CampaignBriefRequest request) {

        CampaignBriefResponse response = campaignBriefService.updateCampaignBrief(id, request);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief updated successfully", response));

    }

    // PUT /api/campaign-briefs/{id}/status - updates only the status of a campaign brief

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> updateCampaignBriefStatus(@PathVariable Integer id, @RequestParam String status) {

        CampaignBriefResponse response = campaignBriefService.updateCampaignBriefStatus(id, status);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief status updated successfully", response));

    }

    // DELETE /api/campaign-briefs/{id} - deletes a campaign brief

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampaignBrief(@PathVariable Integer id) {

        campaignBriefService.deleteCampaignBrief(id);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief deleted successfully", null));

    }

    // Endpoints for Campaign Brief Approval
    
    // POST /api/campaign-briefs/{id}/submit - subimts a brief for approval

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> submitForApproval(@PathVariable Integer id) {

        CampaignBriefResponse response = campaignBriefApprovalService.submitForApproval(id);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief submitted for approval", response));

    }

    // POST /api/campaign-briefs/{id}/decision - records reviewer decision

    @PostMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<CampaignBriefApprovalResponse>> makeDecision(@PathVariable Integer id, @Valid @RequestBody CampaignBriefApprovalRequest request) {

        CampaignBriefApprovalResponse response = campaignBriefApprovalService.makeDecision(id, request);

        return ResponseEntity.ok(ApiResponse.success("Decision recorded successfully", response));

    }

    // POST /api/campaign-briefs/{id}/activate - activates an approved brief

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CampaignBriefResponse>> activateBrief(@PathVariable Integer id) {

        CampaignBriefResponse response = campaignBriefApprovalService.activateBrief(id);

        return ResponseEntity.ok(ApiResponse.success("Campaign brief activated successfully", response));

    }

    // GET /api/campaign-briefs/{id}/approval-history - returns approval history

    @GetMapping("/{id}/approval-history")
    public ResponseEntity<ApiResponse<List<CampaignBriefApprovalResponse>>> getApprovalHistory(@PathVariable Integer id) {

        List<CampaignBriefApprovalResponse> response = campaignBriefApprovalService.getApprovalHistory(id);

        return ResponseEntity.ok(ApiResponse.success("Approval history retrieved successfully", response));

    }
    
}
