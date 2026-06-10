package com.creatorops.organization.service;

import com.creatorops.auth.entity.User;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.common.exception.ResourceNotFoundException;
import com.creatorops.organization.dto.OrganizationRequest;
import com.creatorops.organization.dto.OrganizationResponse;
import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   UserRepository userRepository,
                                   JdbcTemplate jdbcTemplate) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        Organization organization = new Organization(request.name(), request.logoUrl());
        Organization saved = organizationRepository.save(organization);
        return OrganizationResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!id.equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot update an organization outside your own.");
        }

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        organization.setName(request.name());
        organization.setLogoUrl(request.logoUrl());

        Organization updated = organizationRepository.save(organization);
        return OrganizationResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteOrganization(Long id, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!id.equals(user.getOrganizationId())) {
            throw new AccessDeniedException("Access denied: Cannot delete an organization outside your own.");
        }

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));

        // Perform soft delete on organization
        organizationRepository.delete(organization);

        // Cascade soft delete to brands and content physically via JDBC (before they have entities)
        jdbcTemplate.update(
            "UPDATE brand SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE organization_id = ? AND is_deleted = false",
            id
        );
        jdbcTemplate.update(
            "UPDATE content SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE brand_id IN (SELECT id FROM brand WHERE organization_id = ?) AND is_deleted = false",
            id
        );
    }
}
