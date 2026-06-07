# AI Project Context - CreatorOps

> [!IMPORTANT]
> **CRITICAL EXECUTION RULE**  
> Before making any implementation decisions or generating/modifying code, you **MUST** read:
> 1.  **[AI_INSTRUCTIONS.md](file:///S:/Dev/creatorOps/AI_INSTRUCTIONS.md)** (Engineering rulebook)
> 2.  **[AI_PROJECT_CONTEXT.md](file:///S:/Dev/creatorOps/AI_PROJECT_CONTEXT.md)** (This project memory file)
> 3.  **[AI_IMPLEMENTATION_LOG.md](file:///S:/Dev/creatorOps/AI_IMPLEMENTATION_LOG.md)** (Chronological updates & history)
>
> And all referenced project documentation under `docs/`.

---

## 1. Project Overview

*   **Product Name**: CreatorOps
*   **Tagline**: Manage content from idea to publication.
*   **Vision**: CreatorOps is a Content Operations Platform (SaaS) that unifies content planning, research, scriptwriting, assignment coordination, asset tracking, and analytics into a single workspace.

### Product Purpose & Problem Solved
CreatorOps is built to replace fragmented creative workflows. In a typical content team (e.g. SLAY Media), ideas are lost in WhatsApp threads, research links are scattered, scripts reside in unorganized Google Docs, raw assets are uploaded to messy Google Drive folders, and performance analytics are manually copy-pasted into spreadsheets.
CreatorOps centralizes these features to reduce administrative overhead and streamline the pipeline from brainstorming ideas to publishing videos/blogs across multiple sub-brands.

---

## 2. Source of Truth Documents

When details are unclear, refer to these source files under the [docs/](file:///S:/Dev/creatorOps/docs/) directory. **They always take precedence over AI assumptions**:
*   📖 **[Product Requirements Document](file:///S:/Dev/creatorOps/docs/product-requirements.md)**: Personas, permission matrix, state machines, and feature parameters.
*   📐 **[System Architecture](file:///S:/Dev/creatorOps/docs/architecture.md)**: Decoupled containers, code packaging layout, and pluggable AI gateways.
*   🗄️ **[Database Schema Design](file:///S:/Dev/creatorOps/docs/database-design.md)**: Tables catalog, FK structures, soft deletion cascades, indexing.
*   🔌 **[API Design Specification](file:///S:/Dev/creatorOps/docs/api-design.md)**: Path conventions, RFC 7807 global errors, REST actions catalogue.
*   📝 **[Architecture Decision Records](file:///S:/Dev/creatorOps/docs/decisions.md)**: 7 indexed, accepted ADR records mapping constraints and rationales.

---

## 3. Technology Stack

*   **Backend**: Java 21, Spring Boot 3.x, Spring Security, PostgreSQL, Flyway, Maven.
*   **Frontend**: Ember.js Octane (v5.x), TypeScript, Tailwind CSS.
*   **AI Engine**: Google Gemini API via custom adapter (with interface support for OpenAI, Claude, and local Ollama engines).

---

## 4. Domain & Architecture Standards

### 4.1. Domain Tenancy Hierarchy
All data belongs to a strict three-tier structural model:
$$\text{Organization} \longrightarrow \text{Brand} \longrightarrow \text{Content}$$
*   **Organization** (e.g., SLAY Media): The primary tenant. Data isolation is strictly enforced at this level.
*   **Brand** (e.g., SLAY Tech, SLAY Fashion): Sub-divisions belonging to an Organization.
*   **Content**: Individual planning items (videos, podcasts, blog posts) belonging to a Brand.

### 4.2. Role-Based Access Control (RBAC)
*   **ADMIN**: Full system access (manages organization settings, brands, invites users, sets roles, creates content, views analytics).
*   **MANAGER**: Manages the pipeline (creates/assigns content cards, updates workflow stages, reviews drafts, views brand-level analytics). Cannot manage organization properties or modify user roles.
*   **CONTRIBUTOR**: Content execution (views assigned content, writes scripts, links research items, uploads assets, completes checklist tasks, posts comments, and views analytics dashboards). Cannot create content cards, modify assignments, or manage users.

### 4.3. Content Lifecycle States
Content progresses chronologically through these stages:
$$\text{IDEA} \rightarrow \text{RESEARCH} \rightarrow \text{SCRIPT} \rightarrow \text{PRODUCTION} \rightarrow \text{EDITING} \rightarrow \text{REVIEW} \rightarrow \text{SCHEDULED} \rightarrow \text{PUBLISHED}$$
*   *Additional States*: `ON_HOLD` (paused), `CANCELLED` (abandoned).

---

## 5. Core Entities (Logical Catalog)

1.  **Organization**: The tenant root. Includes custom logos (`logo_url`).
2.  **User**: System accounts. Includes custom profile pictures (`image_url`).
3.  **Brand**: Sub-channels. Includes custom logos (`logo_url`).
4.  **Content**: Core planning cards (title, description, type, stage, priority, due dates).
5.  **Assignment**: Associates contributors with specific roles (e.g. Editor, Writer) and statuses (`PENDING`, `IN_PROGRESS`, `COMPLETED`).
6.  **Task**: Checklist items inside content cards (`TODO`, `IN_PROGRESS`, `DONE`).
7.  **ResearchItem**: Context references (`NOTE` text, `LINK` URL, or `AI_BRAINSTORM` output).
8.  **Script**: Holds script metadata and tracks the current version number.
9.  **ScriptVersion**: History record tracking version contents.
10. **Asset**: URL file reference classifying media types (`RAW_VIDEO`, `EDITED_VIDEO`, `THUMBNAIL`, `OTHER`).
11. **Comment**: Threaded developer/creator messages on content.
12. **ActivityLog**: Audits events across organizations chronologically.

---

## 6. Database Standards

*   **Primary Keys**: `BIGINT` auto-incrementing serial sequences.
*   **Table Naming**: Singular lowercase names (e.g. `organization`, `content`).
*   **Column Naming**: Lowercase snake_case (e.g. `created_at`, `is_deleted`).
*   **Timezones**: stored in UTC format via `TIMESTAMPTZ`.
*   **Enum values**: Serialized to database as `VARCHAR(50)`.
*   **Audit Fields**: Mandatory `created_at` and `updated_at` columns automatically updated via database triggers.
*   **Deletions**:
    *   *Soft delete* for root blocks: `organization`, `brand`, and `content`.
    *   *Hard delete* for metadata child collections: `task`, `comment`, `script`, `asset`, `research_item`, `activity_log`.

---

## 7. Key Architecture Decision Records (ADRs) Summary

*   **ADR-001 (BIGINT IDs)**: Chosen over UUIDs to optimize B-Tree indexing speed and minimize page splits in PostgreSQL.
*   **ADR-003 & ADR-004 (Deletion Policy)**: Soft delete safeguards core planning data; hard deletes clean up checklists, versions, and chats using cascade triggers to prevent database bloat.
*   **ADR-005 (VARCHAR Enums)**: Decoupled enums from ordinals to allow inserting stages safely in the future without breaking legacy rows.
*   **ADR-007 (AI Provider Gateway)**: Abstracted interface gateway enables runtime switching between Google Gemini and alternate model adapters.
