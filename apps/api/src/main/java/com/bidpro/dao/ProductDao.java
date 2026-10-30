package com.bidpro.dao;

import com.bidpro.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDao extends JpaRepository<Product, String>, ProductCatalogRepository {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.productID = :productID")
    Optional<Product> findForUpdateByProductID(@Param("productID") String productID);
    @Query("select p.productID from Product p where p.productStatus in :statuses and p.auctionEndDate <= :now and not exists (select n.productID from AuctionEndNotice n where n.productID = p.productID)")
    List<String> findEndedNoticeCandidates(@Param("statuses") List<Product.ProductStatus> statuses, @Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable page);
    @Query("select p.productID from Product p where p.productStatus = :status and p.auctionID is not null and p.auctionEndDate <= :now and exists (select b.bidID from Bid b where b.productID = p.productID) and not exists (select i.invoiceID from Invoice i where i.productID = p.productID) order by p.auctionEndDate, p.productID")
    List<String> findSettlementCandidates(@Param("status") Product.ProductStatus status, @Param("now") java.time.LocalDateTime now, org.springframework.data.domain.Pageable page);
    Optional<Product> findByTitle(String title);
    List<Product> findBySellerUserID(String sellerID);
    List<Product> findByCategoryCategoryID(String categoryID);
    List<Product> findByAuctionAuctionID(String auctionID);
    List<Product> findByProductStatus(Product.ProductStatus status);

    @Query("select p from Product p where p.title like %:keyword% or p.description like %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("select p from Product p where p.startingPrice between :minPrice and :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);
}
