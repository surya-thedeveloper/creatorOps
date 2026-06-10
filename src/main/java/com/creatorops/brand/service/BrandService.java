package com.creatorops.brand.service;

import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.dto.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    Page<BrandResponse> getBrands(String currentUserEmail, Pageable pageable);
    BrandResponse createBrand(String currentUserEmail, BrandRequest request);
    BrandResponse updateBrand(Long id, BrandRequest request, String currentUserEmail);
    void deleteBrand(Long id, String currentUserEmail);
}
