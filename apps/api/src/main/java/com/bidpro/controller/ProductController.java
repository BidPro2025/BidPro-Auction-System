package com.bidpro.controller;
import com.bidpro.dto.*;
import com.bidpro.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service=service; }
    @GetMapping public ApiResponse<List<ProductDTO>> all(@RequestParam(required=false) String keyword,
            @RequestParam(required=false) String categoryID) {
        var data=keyword==null ? service.getPublicProducts() : service.searchProducts(keyword);
        if (categoryID!=null && !categoryID.isBlank()) data=data.stream().filter(p->categoryID.equals(p.getCategoryID())).toList();
        return ApiResponse.success(data,"Products retrieved");
    }
    @GetMapping("/mine") public ApiResponse<List<ProductDTO>> mine() { return ApiResponse.success(service.findMine(),"Your listings"); }
    @PostMapping public ResponseEntity<ApiResponse<ProductDTO>> create(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.createProduct(dto,null),"Listing submitted for approval"));
    }
    @GetMapping("/{id}") public ApiResponse<ProductDTO> one(@PathVariable String id) { return ApiResponse.success(service.findProductById(id),"Product retrieved"); }
    @GetMapping("/category/{id}") public ApiResponse<List<ProductDTO>> category(@PathVariable String id) { return ApiResponse.success(service.findProductsByCategory(id),"Products retrieved"); }
    @GetMapping("/search") public ApiResponse<ProductService.CatalogPage> search(
            @RequestParam(defaultValue="") String q, @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue="") String category, @RequestParam(defaultValue="") String status,
            @RequestParam(defaultValue="ending") String sort, @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="12") int size) {
        return ApiResponse.success(service.searchCatalog(q.isBlank() && keyword!=null ? keyword : q,
                category,status,sort,page,size),"Catalog page retrieved");
    }
    @PutMapping("/{id}") public ApiResponse<ProductDTO> update(@PathVariable String id,@Valid @RequestBody ProductDTO dto) { return ApiResponse.success(service.updateProduct(id,dto,null),"Listing updated"); }
    @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable String id) { service.deleteProduct(id); return ApiResponse.success(null,"Listing deleted"); }
}
