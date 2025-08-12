package com.bidpro.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {
    @Id
    @Column(name = "categoryID", length = 50)
    private String categoryID;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "visibility", nullable = false)
    private Boolean visibility;

    @Column(name = "displayOrder")
    private Integer displayOrder;

    @Column(name = "parentCategory", length = 50)
    private String parentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentCategory", referencedColumnName = "categoryID", insertable = false, updatable = false)
    private Category parent;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;

    public Category() {
        this.visibility = true;
    }
}
