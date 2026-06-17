package com.creatorops.brand.controller;

import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.dto.BrandResponse;
import com.creatorops.brand.service.BrandService;
import com.creatorops.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Brands", description = "Manage sub-channel brands within an organization. Scoped to the caller's organization.")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    @Operation(summary = "List brands", description = "Returns a paginated list of all brands in the caller's organization.")
    @ApiResponse(responseCode = "200", description = "Brands listed successfully")
    public ResponseEntity<PagedResponse<BrandResponse>> getBrands(
            Authentication authentication,
            Pageable pageable) {
        Page<BrandResponse> responsePage = brandService.getBrands(authentication.getName(), pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(responsePage));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create brand", description = "Creates a new brand under the caller's organization. ADMIN only.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Brand created"),
        @ApiResponse(responseCode = "400", description = "Validation failure")
    })
    public ResponseEntity<BrandResponse> createBrand(
            Authentication authentication,
            @Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update brand", description = "Updates name, description, or logo of a brand.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Brand updated"),
        @ApiResponse(responseCode = "404", description = "Brand not found")
    })
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request,
            Authentication authentication) {
        BrandResponse response = brandService.updateBrand(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete brand", description = "Soft-deletes a brand. Existing content is retained.")
    @ApiResponse(responseCode = "204", description = "Brand deleted")
    public ResponseEntity<Void> deleteBrand(
            @PathVariable Long id,
            Authentication authentication) {
        brandService.deleteBrand(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
