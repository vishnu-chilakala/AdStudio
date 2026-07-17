package com.cts.creative.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "creative_assets",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "assetName",
                "brandId"
            }
        )
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreativeAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    private Long brandId;

    private Long campaignBriefId;

    private String assetName;

    private String filePath;

    private Integer fileSizeKB;

    private Integer version;

    private Long uploadedById;

    private Integer width;

    private Integer height;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonIgnore
    @OneToMany(
        mappedBy = "asset",
        cascade = CascadeType.ALL
    )
    private List<CreativeApproval> approvals;

    @JsonIgnore
    @OneToMany(
        mappedBy = "asset",
        cascade = CascadeType.ALL
    )
    private List<AssetLineItemLink> links;

    public enum Status {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        ARCHIVED
    }

    public enum AssetType {
        BANNER,
        VIDEO,
        IMAGE,
        NATIVE,
        AUDIO,
        RICH_MEDIA
    }
}