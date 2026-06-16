package com.creatorops.organization.repository;

import com.creatorops.organization.entity.Organization;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Override
    @Cacheable(value = "organizations", key = "#id")
    Optional<Organization> findById(Long id);
}
