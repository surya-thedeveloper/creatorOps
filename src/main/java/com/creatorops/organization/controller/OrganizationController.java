package com.creatorops.organization.controller;

import com.creatorops.organization.dto.OrganizationRequest;
import com.creatorops.organization.dto.OrganizationResponse;
import com.creatorops.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Organizations", description = "Manage the tenant root organization. ADMIN role required for all operations.")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create organization", description = "Creates a new tenant organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Organization created"),
        @ApiResponse(responseCode = "400", description = "Validation failure")
    })
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization", description = "Updates name or logo of the organization. Caller must belong to the organization.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Organization updated"),
        @ApiResponse(responseCode = "403", description = "Not owner of organization"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request,
            Authentication authentication) {
        OrganizationResponse response = organizationService.updateOrganization(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete organization", description = "Soft-deletes the organization. Sets is_deleted=true, data is retained.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Organization deleted"),
        @ApiResponse(responseCode = "403", description = "Not owner of organization"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        organizationService.deleteOrganization(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
