package com.cts.creative.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cts.creative.entity.CreativeAsset;
import com.cts.creative.service.CreativeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/creative-assets")
@RequiredArgsConstructor
public class CreativeAssetController {

    private final CreativeService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(

            @RequestParam("file")
            MultipartFile file,

            @RequestParam Long brandId,

            @RequestParam Long campaignBriefId,

            @RequestParam String assetName,

            @RequestParam Long uploadedById,

            @RequestParam CreativeAsset.AssetType assetType,

            @RequestParam Integer width,

            @RequestParam Integer height

    ) throws Exception {

        return ResponseEntity.ok(
                service.upload(
                        file,
                        brandId,
                        campaignBriefId,
                        assetName,
                        uploadedById,
                        assetType,
                        width,
                        height
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getAll() {

        return ResponseEntity.ok(
                service.getAllAssets()
        );
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<?> getById(
            @PathVariable Long assetId) {

        return ResponseEntity.ok(
                service.getAsset(assetId)
        );
    }

    @PutMapping(
            value = "/{assetId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> update(

            @PathVariable Long assetId,

            @RequestParam(required = false)
            MultipartFile file,

            @RequestParam String assetName,

            @RequestParam CreativeAsset.AssetType assetType,

            @RequestParam Integer width,

            @RequestParam Integer height

    ) throws Exception {

        return ResponseEntity.ok(
                service.updateAsset(
                        assetId,
                        file,
                        assetName,
                        assetType,
                        width,
                        height
                )
        );
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<?> delete(
            @PathVariable Long assetId) {

        service.deleteAsset(assetId);

        return ResponseEntity.ok(
                "Asset Deleted Successfully"
        );
    }
}