package com.bidpro.entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name = "auction_end_notice") @Getter @Setter
public class AuctionEndNotice {
    @Id @Column(length=50) private String productID;
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
}
