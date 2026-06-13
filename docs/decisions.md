# Architecture Decision Records (ADR)

This document catalogs the key architectural decisions, context, trade-offs, and consequences accepted during the design of **CreatorOps**.

---

## Index of Decisions

1.  **[ADR-001: Use BIGINT for Primary Keys](#adr-001-use-bigint-for-primary-keys)**
2.  **[ADR-002: Multi-Tenant Hierarchy (Organization $\rightarrow$ Brand $\rightarrow$ Content)](#adr-002-multi-tenant-hierarchy-organization--brand--content)**
3.  **[ADR-003: Soft Delete for Core Tenancy and Domain Cards](#adr-003-soft-delete-for-core-tenancy-and-domain-cards)**
4.  **[ADR-004: Hard Delete for Child Context Metadata](#adr-004-hard-delete-for-child-context-metadata)**
5.  **[ADR-005: Store Enums as VARCHAR in PostgreSQL](#adr-005-store-enums-as-varchar-in-postgresql)**
6.  **[ADR-006: Method-Level RBAC with Spring Security](#adr-006-method-level-rbac-with-spring-security)**
7.  **[ADR-007: Interface-Based AI Provider Abstraction Gateway](#adr-007-interface-based-ai-provider-abstraction-gateway)**
8.  **[ADR-008: Hybrid Script Editing Strategy](#adr-008-hybrid-script-editing-strategy)**

---

## ADR-001: Use BIGINT for Primary Keys

### Status
Accepted

### Context
We need to determine the primary key strategy for all database tables. The primary options are 32-bit Integer (`INT`), 64-bit Integer (`BIGINT`), and Universally Unique Identifiers (`UUIDv4` or `UUIDv7`).

### Decision
We will use 64-bit unsigned/signed integers (`BIGINT` in PostgreSQL mapping to `Long` in Java) generated via auto-increment database identity sequences (`IDENTITY`) for all primary keys.

### Consequences
*   **Pros**:
    *   Significant performance benefits over UUIDs in index structures (B-Tree index ordering is faster with sequential integers).
    *   Reduced storage overhead (8 bytes per row vs 16 bytes for UUIDs).
    *   Easier debugging and readability during developer troubleshooting.
*   **Cons**:
    *   Endpoints expose sequential IDs (e.g. `/api/contents/42`), which could reveal scale. We mitigate this using authorization filters to ensure tenants can never access IDs outside their scope, and we can introduce hashids in URLs if public obfuscation is required.

---

## ADR-002: Multi-Tenant Hierarchy (Organization $\rightarrow$ Brand $\rightarrow$ Content)

### Status
Accepted

### Context
Creator operations teams range from solo influencers to large multi-brand production networks (e.g., SLAY Media managing SLAY Fashion, SLAY Tech, and SLAY Fitness). The system must allow users to cross-collaborate while enforcing data boundaries.

### Decision
We will enforce a strict 3-tier logical ownership hierarchy:
*   An **Organization** represents the billing entity and top-level database partition. Users are invited to an Organization.
*   A **Brand** represents a media channel or sub-division. Brands belong to one Organization.
*   **Content** cards belong to one Brand.

### Consequences
*   **Pros**:
    *   Allows large teams to partition workspace screens by Brand (reducing visual clutter).
    *   Enables simple tenant isolation at the Organization level by appending a global tenant filter (`organization_id`) on active database transactions.
*   **Cons**:
    *   Slightly more complex query joins when retrieving all content under an entire Organization (requires joining `content` $\rightarrow$ `brand` $\rightarrow$ `organization`).

---

## ADR-003: Soft Delete for Core Tenancy and Domain Cards

### Status
Accepted

### Context
Accidental deletions of Organizations, Brands, or high-value Content cards containing scripts and research logs can ruin creator pipelines. We need a recovery strategy.

### Decision
We will implement Column-Level Soft Deletion for:
*   `organization`
*   `brand`
*   `content`

We will add fields `is_deleted` (`BOOLEAN`, default false) and `deleted_at` (`TIMESTAMPTZ`, null by default) to these tables.

### Consequences
*   **Pros**:
    *   Provides protection against accidental user deletions, allowing admins to restore soft-deleted items.
    *   Maintains historical integrity in database analytics.
*   **Cons**:
    *   Queries must filter out deleted records, increasing database complexity. We will use Hibernate `@Where(clause = "is_deleted = false")` on entities to automate this.
    *   Unique constraint collisions can occur (e.g. creating a brand with the same name as a deleted brand). We will handle this in application logic.

---

## ADR-004: Hard Delete for Child Context Metadata

### Status
Accepted

### Context
While core assets are soft deleted, secondary elements such as specific task checklists, comments, asset URLs, and research snippets do not need safety holds. Accumulating orphan rows from soft-deleted children bloat databases.

### Decision
We will hard delete (physically remove from disk) supporting child models:
*   `task`
*   `comment`
*   `script` and `script_version`
*   `asset`
*   `activity_log`
*   `research_item`

We will define Foreign Keys with `ON DELETE CASCADE`.

### Consequences
*   **Pros**:
    *   Keeps database size lean and avoids database vacuum degradation.
    *   Requires no active application filtering logic for secondary tables.
*   **Cons**:
    *   Deletions of these components are irreversible. If a user deletes a comment, it is permanently lost.

---

## ADR-005: Store Enums as VARCHAR in PostgreSQL

### Status
Accepted

### Context
Spring Boot applications use Java Enums for tracking states (e.g., content `stage`, asset `type`). By default, JPA Hibernate maps Enums to database fields as integers (ordinal values) or custom PostgreSQL custom types.

### Decision
We will store all Enums as standard `VARCHAR(50)` strings in PostgreSQL.

### Consequences
*   **Pros**:
    *   Adding new stages to the enum lifecycle (e.g., adding `ARCHIVED` between `IDEA` and `PUBLISHED`) does not disrupt existing database data. Ordinals are highly fragile when modified.
    *   Allows raw SQL debugging directly in database consoles without translating integers.
*   **Cons**:
    *   Slightly increased storage footprint (strings vs 4-byte integers), but this is negligible at our projected scale.

---

## ADR-006: Method-Level RBAC with Spring Security

### Status
Accepted

### Context
We must restrict system routes and operations based on user roles (`ADMIN`, `MANAGER`, `CONTRIBUTOR`). Security checks should happen consistently across the codebase.

### Decision
We will enforce Role-Based Access Control (RBAC) globally using **Spring Security method-level annotations** (`@PreAuthorize`) on REST controllers.

### Consequences
*   **Pros**:
    *   Keeps security definitions directly adjacent to the controller code, making developer validation easier.
    *   Ensures that even if endpoint filters are bypassed, methods cannot execute without correct user scopes.
*   **Cons**:
    *   Spreads authorization logic across many files. To prevent inconsistencies, we will also create custom validation meta-annotations (e.g., `@IsAdmin`, `@IsManager`).

---

## ADR-007: Interface-Based AI Provider Abstraction Gateway

### Status
Accepted

### Context
CreatorOps relies heavily on LLM capabilities for script generation, hook recommendations, and summaries. Our default provider is Google Gemini, but we want flexibility to use OpenAI, Claude, or local Ollama engines in the future without rewritten domain services.

### Decision
We will define an interface-driven gateway pattern (`AiProviderGateway`). All script-writing business services will interact exclusively with this interface. Concrete adapters (e.g., `GeminiProviderAdapter`) will implement details.

### Consequences
*   **Pros**:
    *   Decoupled architecture. We can switch providers via environment configuration changes.
    *   Supports unit testing by allowing developers to mock the AI provider with static responses.
*   **Cons**:
    *   Standardizing request and response models across providers can be complex, as different engines return data in different shapes (e.g., function calling schemas).

---

## ADR-008: Hybrid Script Editing Strategy

### Status
Accepted

### Context
Creator teams use different tools and have varied preferences for scriptwriting workflows. Forcing creators to migrate entirely to a new in-app editor causes high friction, while relying solely on external tools fragments content status tracking and separates script drafts from research inputs and stage flows.

### Decision
We will support a hybrid script editing strategy within the Script Workspace:
1. **Internal Editor**: Provide a basic rich-text editor (supporting Headings, Bold, Italic, Underline, Bullet/Numbered Lists) directly in CreatorOps.
2. **External Pointers**: Allow users to configure external document links (Google Docs, Microsoft Word Online) or upload document files (`.docx`) to act as the source of truth reference.
3. **AI Draft Generation**: Build a unified AI generator that parses research items (Notes, Links, AI Brainstorm items) to produce a baseline draft (Version 1) available for copying or direct editing.

### Consequences
*   **Pros**:
    *   Accommodates diverse workflow preferences, ensuring rapid adoption and low friction.
    *   Allows teams to utilize Google Docs or MS Word features without losing status tracking in CreatorOps.
    *   Keeps initial database schema simple by storing links/file references rather than complex real-time collaboration states.
*   **Cons**:
    *   Does not support automatic bi-directional document sync or live multi-user collaborative editing inside CreatorOps in Phase 1 (deferred to future phases).
    *   Requires users to manually copy/paste drafts if they choose external editors without active API syncs.
