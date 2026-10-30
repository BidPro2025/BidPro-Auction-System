package com.bidpro.dao;

import com.bidpro.entities.Bid;
import com.bidpro.entities.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/** Public filtering and limit/offset ordering happen in the database, never in memory. */
public class ProductCatalogRepositoryImpl implements ProductCatalogRepository {
    private final EntityManager entityManager;

    public ProductCatalogRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Product> searchPublicCatalog(String query, String category, String status,
            String sort, LocalDateTime now, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(builder.count(countRoot)).where(filters(builder, countRoot, query, category, status, now));
        long total = entityManager.createQuery(countQuery).getSingleResult();
        if (total == 0 || pageable.getOffset() >= total) return new PageImpl<>(List.of(), pageable, total);

        CriteriaQuery<Product> resultQuery = builder.createQuery(Product.class);
        Root<Product> root = resultQuery.from(Product.class);
        resultQuery.select(root).where(filters(builder, root, query, category, status, now));
        List<Order> ordering = new ArrayList<>();
        if ("newest".equals(sort)) {
            ordering.add(builder.desc(root.get("createdAt")));
        } else if ("price-low".equals(sort) || "price-high".equals(sort)) {
            Subquery<BigDecimal> highestBid = resultQuery.subquery(BigDecimal.class);
            Root<Bid> bid = highestBid.from(Bid.class);
            highestBid.select(builder.max(bid.<BigDecimal>get("amount")))
                    .where(builder.equal(bid.get("productID"), root.get("productID")));
            Expression<BigDecimal> currentPrice = builder.coalesce(highestBid, root.<BigDecimal>get("startingPrice"));
            ordering.add("price-low".equals(sort) ? builder.asc(currentPrice) : builder.desc(currentPrice));
        } else {
            Expression<Integer> activeFirst = builder.<Integer>selectCase()
                    .when(builder.and(builder.equal(root.get("productStatus"), Product.ProductStatus.APPROVED),
                            builder.greaterThan(root.get("auctionEndDate"), now)), 0).otherwise(1);
            ordering.add(builder.asc(activeFirst));
            ordering.add(builder.asc(root.get("auctionEndDate")));
        }
        ordering.add(builder.asc(root.get("productID")));
        resultQuery.orderBy(ordering);
        List<Product> items = entityManager.createQuery(resultQuery)
                .setFirstResult(Math.toIntExact(pageable.getOffset()))
                .setMaxResults(pageable.getPageSize()).getResultList();
        return new PageImpl<>(items, pageable, total);
    }

    private Predicate[] filters(CriteriaBuilder builder, Root<Product> product, String query,
            String category, String status, LocalDateTime now) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(product.get("productStatus").in(Product.ProductStatus.APPROVED, Product.ProductStatus.SOLD));
        if (!query.isEmpty()) {
            String literal = query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            String contains = "%" + literal + "%";
            predicates.add(builder.or(builder.like(builder.lower(product.get("title")), contains, '\\'),
                    builder.like(builder.lower(product.get("description")), contains, '\\')));
        }
        if (!category.isEmpty()) predicates.add(builder.equal(product.get("categoryID"), category));
        if ("SOLD".equals(status)) {
            predicates.add(builder.equal(product.get("productStatus"), Product.ProductStatus.SOLD));
        } else if (!status.isEmpty()) {
            predicates.add(builder.equal(product.get("productStatus"), Product.ProductStatus.APPROVED));
            if ("UPCOMING".equals(status)) predicates.add(builder.greaterThan(product.get("auctionStartDate"), now));
            else if ("ENDED".equals(status)) predicates.add(builder.lessThanOrEqualTo(product.get("auctionEndDate"), now));
            else {
                predicates.add(builder.lessThanOrEqualTo(product.get("auctionStartDate"), now));
                predicates.add(builder.greaterThan(product.get("auctionEndDate"), now));
            }
        }
        return predicates.toArray(Predicate[]::new);
    }
}
