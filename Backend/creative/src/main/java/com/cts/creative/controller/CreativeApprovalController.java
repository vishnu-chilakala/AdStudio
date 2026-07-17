package com.cts.creative.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.creative.dto.ApprovalRequest;
import com.cts.creative.service.CreativeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/creative-approvals")
@RequiredArgsConstructor
public class CreativeApprovalController {

    private final CreativeService service;

    @PutMapping("/{assetId}/decision")
    public ResponseEntity<?> approve(

            @PathVariable Long assetId,

            @Valid
            @RequestBody
            ApprovalRequest request) {

        return ResponseEntity.ok(
                service.approveAsset(
                        assetId,
                        request));
    }
}