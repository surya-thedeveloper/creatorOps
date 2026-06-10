package com.creatorops.organization.service;

import com.creatorops.organization.dto.OrganizationRequest;
import com.creatorops.organization.dto.OrganizationResponse;

public interface OrganizationService {
    OrganizationResponse createOrganization(OrganizationRequest request);
    OrganizationResponse updateOrganization(Long id, OrganizationRequest request, String currentUserEmail);
    void deleteOrganization(Long id, String currentUserEmail);
}
