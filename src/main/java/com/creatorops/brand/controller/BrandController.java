package com.creatorops.brand.controller;

import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.dto.BrandResponse;
import com.creatorops.brand.service.BrandService;
import com.creatorops.common.response.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<BrandResponse>> getBrands(
            Authentication authentication,
            Pageable pageable) {
        Page<BrandResponse> responsePage = brandService.getBrands(authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(responsePage));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(
            Authentication authentication,
            @Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request,
            Authentication authentication) {
        BrandResponse response = brandService.updateBrand(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id,
            Authentication authentication) {
        brandService.deleteBrand(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
