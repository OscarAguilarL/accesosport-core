package com.grupocaos.products.athletix.user.infrastructure.persistence.entity;

import com.grupocaos.products.athletix.shared.domain.valueobjects.VerificationStatus;
import com.grupocaos.products.athletix.shared.infrastructure.common.persistence.jpa.AddressEmbeddable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a JPA entity for storing organizer profile information associated with a user.
 * This entity includes details about the organization and its verification status,
 * as well as metadata for tracking creation and update timestamps.
 * The entity is mapped to the "organizer_profile" table in the database.
 * It uses a JOINED inheritance strategy and embeds an address entity.
 * Relationships and lifecycle callbacks are also defined for this entity.
 */
@Entity
@Table(name = "organizer_profile")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOrganizerProfileJpaEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String organizationName;

    @Column(nullable = false, length = 120)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Embedded
    private AddressEmbeddable address;

    @Column(length = 150)
    private String website;

    @Column(length = 150)
    private String facebook;

    @Column(length = 150)
    private String instagram;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Column(nullable = true)
    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
