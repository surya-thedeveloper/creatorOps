package com.creatorops.brand.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.brand.dto.BrandRequest;
import com.creatorops.brand.dto.BrandResponse;
import com.creatorops.brand.entity.Brand;
import com.creatorops.brand.repository.BrandRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.organization.entity.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BrandServiceImpl(BrandRepository brandRepository,
                            UserRepository userRepository,
                            JdbcTemplate jdbcTemplate) {
        this.brandRepository = brandRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brands")
    public Page<BrandResponse> getBrands(String currentUserEmail, Pageable pageable) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return brandRepository.findByOrganization_Id(user.getOrganizationId(), pageable)
                .map(BrandResponse::fromEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandResponse createBrand(String currentUserEmail, BrandRequest request) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization org = user.getOrganization();
        if (org == null) {
            throw new ResourceNotFoundException("Organization not found for the user");
        }

        Brand brand = new Brand(request.name(), request.description(), request.logoUrl(), org);
        Brand saved = brandRepository.save(brand);
        return BrandResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandResponse updateBrand(Long id, BrandRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (!brand.getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot update a brand outside your organization.");
        }

        brand.setName(request.name());
        brand.setDescription(request.description());
        brand.setLogoUrl(request.logoUrl());

        Brand updated = brandRepository.save(brand);
        return BrandResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (!brand.getOrganizationId().equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot delete a brand outside your organization.");
        }

        brandRepository.delete(brand);

        // Cascade soft delete to content cards physically under this brand
        jdbcTemplate.update(
            "UPDATE content SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE brand_id = ? AND is_deleted = false",
            id
        );
    }
}
