package com.bidpro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO extends BaseDTO {
    @Size(max = 50, message = "Category ID must be at most 50 characters")
    private String categoryID;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    private String description;

    private Boolean visibility;

    private Integer displayOrder;

    @Size(max = 50, message = "Parent Category ID must be at most 50 characters")
    private String parentCategory;
}