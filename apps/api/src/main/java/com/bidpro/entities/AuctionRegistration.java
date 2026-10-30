package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "auction_registration")
@Getter
@Setter
public class AuctionRegistration {

    @EmbeddedId
    private AuctionRegistrationId id;

    @ManyToOne
    @MapsId("auctionID")
    @JoinColumn(name = "auctionID", nullable = false)
    private Auction auction;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "registrationStatus", nullable = false)
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @Column(name = "registeredAt", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "extension1", length = 255)
    private String extension1;

    @Column(name = "extension2", length = 255)
    private String extension2;

    // Inner enum for registration status
    public enum RegistrationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Embeddable
    public static class AuctionRegistrationId implements Serializable {
        @Column(name = "auctionID")
        private String auctionID;

        @Column(name = "userID")
        private String userID;

        public String getAuctionID() {
            return auctionID;
        }

        public void setAuctionID(String auctionID) {
            this.auctionID = auctionID;
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(String userID) {
            this.userID = userID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuctionRegistrationId that = (AuctionRegistrationId) o;
            return Objects.equals(auctionID, that.auctionID) && Objects.equals(userID, that.userID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(auctionID, userID);
        }
    }

    @PrePersist
    public void prePersist() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}