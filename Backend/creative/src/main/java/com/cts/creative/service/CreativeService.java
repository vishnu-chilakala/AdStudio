package com.cts.creative.service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cts.creative.dto.ApprovalRequest;
import com.cts.creative.dto.AssetLinkRequest;
import com.cts.creative.entity.AssetLineItemLink;
import com.cts.creative.entity.CreativeApproval;
import com.cts.creative.entity.CreativeAsset;
import com.cts.creative.creativeexception.CreativeNotFoundException;
import com.cts.creative.repository.AssetLineItemLinkRepository;
import com.cts.creative.repository.CreativeApprovalRepository;
import com.cts.creative.repository.CreativeAssetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreativeService {

    private final CreativeAssetRepository assetRepo;
    private final CreativeApprovalRepository approvalRepo;
    private final AssetLineItemLinkRepository linkRepo;

    @Value("${creative.upload.path}")
    private String uploadPath;

    // UPLOAD CREATIVE ASSET
    public CreativeAsset upload(
            MultipartFile file,
            Long brandId,
            Long campaignBriefId,
            String assetName,
            Long uploadedById,
            CreativeAsset.AssetType assetType,
            Integer width,
            Integer height
    ) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }

        File dir = new File(uploadPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName =
                System.currentTimeMillis()
                        + "_"
                        + file.getOriginalFilename()
                        .replaceAll("\\s+", "_");

        String filePath = uploadPath + fileName;

        file.transferTo(new File(filePath));

        CreativeAsset asset =
                CreativeAsset.builder()
                        .brandId(brandId)
                        .campaignBriefId(campaignBriefId)
                        .assetName(assetName)
                        .uploadedById(uploadedById)
                        .assetType(assetType)
                        .width(width)
                        .height(height)
                        .filePath(filePath)
                        .fileSizeKB((int) (file.getSize() / 1024))
                        .version(1)
                        .status(CreativeAsset.Status.DRAFT)
                        .build();

        return assetRepo.save(asset);
    }

    // GET ALL ASSETS
    public List<CreativeAsset> getAllAssets() {
        return assetRepo.findAll();
    }

    // GET ASSET BY ID
    public CreativeAsset getAsset(Long assetId) {

        return assetRepo.findById(assetId)
                .orElseThrow(() ->
                        new CreativeNotFoundException(
                                "Asset Not Found"));
    }

    // UPDATE ASSET
    public CreativeAsset updateAsset(
            Long assetId,
            MultipartFile file,
            String assetName,
            CreativeAsset.AssetType assetType,
            Integer width,
            Integer height
    ) throws Exception {

        CreativeAsset asset =
                assetRepo.findById(assetId)
                        .orElseThrow(() ->
                                new CreativeNotFoundException(
                                        "Asset Not Found"));

        asset.setAssetName(assetName);
        asset.setAssetType(assetType);
        asset.setWidth(width);
        asset.setHeight(height);

        if (file != null && !file.isEmpty()) {

            if (asset.getFilePath() != null) {

                File oldFile =
                        new File(asset.getFilePath());

                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + file.getOriginalFilename()
                            .replaceAll("\\s+", "_");

            String filePath =
                    uploadPath + fileName;

            file.transferTo(new File(filePath));

            asset.setFilePath(filePath);

            asset.setFileSizeKB(
                    (int) (file.getSize() / 1024));

            asset.setVersion(
                    asset.getVersion() + 1);
        }

        return assetRepo.save(asset);
    }

    // DELETE ASSET
    public void deleteAsset(Long assetId) {

        CreativeAsset asset =
                assetRepo.findById(assetId)
                        .orElseThrow(() ->
                                new CreativeNotFoundException(
                                        "Asset Not Found"));

        if (asset.getLinks() != null
                && !asset.getLinks().isEmpty()) {

            throw new RuntimeException(
                    "Asset is linked with line items and cannot be deleted");
        }

        assetRepo.delete(asset);
    }

    // APPROVE / REJECT ASSET
    public CreativeApproval approveAsset(
            Long assetId,
            ApprovalRequest request) {

        CreativeAsset asset =
                assetRepo.findById(assetId)
                        .orElseThrow(() ->
                                new CreativeNotFoundException(
                                        "Asset Not Found"));

        if ("Approved".equalsIgnoreCase(
                request.getDecision())) {

            asset.setStatus(
                    CreativeAsset.Status.APPROVED);

        } else {

            asset.setStatus(
                    CreativeAsset.Status.REJECTED);
        }

        assetRepo.save(asset);

        CreativeApproval approval =
                CreativeApproval.builder()
                        .asset(asset)
                        .reviewerId(
                                request.getReviewerId())
                        .reviewDate(
                                LocalDate.now())
                        .decision(
                                request.getDecision())
                        .feedback(
                                request.getFeedback())
                        .status("COMPLETED")
                        .build();

        return approvalRepo.save(approval);
    }

    // LINK ASSET TO LINE ITEM
    public AssetLineItemLink linkAsset(
            AssetLinkRequest request) {

        CreativeAsset asset =
                assetRepo.findById(
                        request.getAssetId())
                        .orElseThrow(() ->
                                new CreativeNotFoundException(
                                        "Asset Not Found"));

        if (asset.getStatus()
                != CreativeAsset.Status.APPROVED) {

            throw new RuntimeException(
                    "Only Approved Assets can be linked");
        }

        if (linkRepo.existsByAssetAndLineItemId(
                asset,
                request.getLineItemId())) {

            throw new RuntimeException(
                    "Duplicate Link Not Allowed");
        }

        AssetLineItemLink link =
                AssetLineItemLink.builder()
                        .asset(asset)
                        .lineItemId(
                                request.getLineItemId())
                        .linkedDate(LocalDate.now())
                        .status("ACTIVE")
                        .build();

        return linkRepo.save(link);
    }
}