package com.cts.creative.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.creative.dto.AssetLinkRequest;
import com.cts.creative.service.CreativeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/asset-links")
@RequiredArgsConstructor
public class AssetLinkController {

    private final CreativeService service;

    @PostMapping
    public ResponseEntity<?> createLink(

            @Valid
            @RequestBody
            AssetLinkRequest request) {

        return ResponseEntity.ok(
                service.linkAsset(request));
    }
}