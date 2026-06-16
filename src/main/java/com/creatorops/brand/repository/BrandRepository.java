package com.creatorops.brand.repository;

import com.creatorops.brand.entity.Brand;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Page<Brand> findByOrganization_Id(Long organizationId, Pageable pageable);

    @Override
    @Cacheable(value = "brands", key = "#id")
    Optional<Brand> findById(Long id);
}
