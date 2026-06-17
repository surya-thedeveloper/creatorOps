# API Design Specification

This document details the REST API design conventions, authorization integration, response structures, and a comprehensive endpoint catalog for the **CreatorOps** platform.

---

## 1. REST Conventions & Standards

The CreatorOps API is built following standard RESTful design practices:

1.  **Transport Protocol**: All API communication must be encrypted over HTTPS.
2.  **Resource Naming**: Plural nouns for resource identifiers (e.g., `/api/v1/brands`, `/api/v1/contents`).
3.  **HTTP Verbs**:
    *   `GET`: Retrieve a resource or a list of resources. Safe and idempotent.
    *   `POST`: Create a new resource. Non-idempotent.
    *   `PUT`: Update an existing resource. Idempotent.
    *   `DELETE`: Remove a resource. Idempotent.
4.  **Content-Type**:
    *   Request payload format: `application/json`
    *   Response payload format: `application/json`
5.  **Audit Headers**: All requests are checked for tenant context through user identity validation inside the JWT header.

---

## 2. Authentication & Authorization

All requests to secured endpoints must supply an Authentication header containing a valid JSON Web Token (JWT) Bearer token.

### Header Format
```http
Authorization: Bearer <JWT_TOKEN>
```

### Authentication Lifecycle
*   **Token Retrieval**: Clients request tokens by providing email credentials to the public login endpoint `/api/v1/auth/login`.
*   **Token Duration**: Access tokens are signed using SHA-256 HMAC and have a validity period of 24 hours.
*   **Claims Structure**:
    ```json
    {
      "sub": "123456",
      "name": "Surya",
      "email": "surya@example.com",
      "orgId": "987654",
      "role": "ADMIN",
      "exp": 1780833600
    }
    ```

---

## 3. Global Response Formats

### 3.1. Single Resource Wrap
Single entity responses return the resource properties directly in the JSON root.
```json
{
  "id": 1029,
  "title": "Unboxing the New AI Chipset",
  "type": "YOUTUBE_VIDEO",
  "stage": "IDEA",
  "priority": "HIGH",
  "dueDate": "2026-07-01T12:00:00Z"
}
```

### 3.2. Collection Responses (Pagination & Sorting)
List endpoints return resources grouped within a `content` array alongside metadata keys tracking pagination details.
*   **Default Page Size**: 20 records.
*   **Query Parameters**:
    *   `page`: Page index (0-indexed). E.g., `page=0`
    *   `size`: Number of records per page. E.g., `size=10`
    *   `sort`: Sort property and direction format: `property,asc` or `property,desc`. E.g., `sort=dueDate,desc`

```json
{
  "content": [
    {
      "id": 1029,
      "title": "Unboxing the New AI Chipset",
      "type": "YOUTUBE_VIDEO",
      "stage": "IDEA",
      "priority": "HIGH"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 1,
    "totalElements": 45,
    "totalPages": 45,
    "isLast": false
  }
}
```

### 3.3. Error Responses (RFC 7807 Problem Details)
CreatorOps uses the standard **RFC 7807** specification to return clean, actionable error models.
*   **Failed Validation (400 Bad Request)**:
    ```json
    {
      "type": "https://api.creatorops.com/errors/validation-failed",
      "title": "Validation Failed",
      "status": 400,
      "detail": "One or more inputs in your payload failed safety validations.",
      "instance": "/api/v1/contents",
      "errors": [
        {
          "field": "title",
          "message": "Title cannot be blank"
        }
      ]
    }
    ```
*   **Unauthorized Access (403 Forbidden)**:
    ```json
    {
      "type": "https://api.creatorops.com/errors/forbidden",
      "title": "Access Denied",
      "status": 403,
      "detail": "You do not have permission to view this resource.",
      "instance": "/api/v1/organizations/1/brands"
    }
    ```

---

## 4. Endpoint Catalog

### 4.1. Auth Endpoint (Public)
*   **`POST /api/v1/auth/login`**: Validate credentials and fetch a JWT token.
    *   *Request Payload*:
        ```json
        {
          "email": "user@example.com",
          "password": "securepassword"
        }
        ```
    *   *Response (200 OK)*:
        ```json
        {
          "token": "eyJhbGciOi...",
          "user": {
            "id": 1,
            "name": "Surya",
            "email": "user@example.com",
            "role": "ADMIN",
            "imageUrl": "https://example.com/avatars/surya.png",
            "organizationId": 10
          }
        }
        ```

### 4.2. Organization Management (Admin only)
*   **`POST /api/v1/organizations`**: Register a new Organization.
    *   *Request Payload*:
        ```json
        {
          "name": "SLAY Media Group",
          "logoUrl": "https://example.com/logos/slay-media.png"
        }
        ```
*   **`PUT /api/v1/organizations/{id}`**: Edit details of an Organization.
*   **`DELETE /api/v1/organizations/{id}`**: Soft delete an Organization.

### 4.3. Brand Management
*   **`GET /api/v1/brands`**: Fetch all active brands under the user's Organization.
*   **`POST /api/v1/brands`**: Create a new Brand (Admin only).
    *   *Request Payload*:
        ```json
        {
          "name": "SLAY Fashion",
          "description": "Fashion guides, hauls, and styling tips",
          "logoUrl": "https://example.com/logos/slay-fashion.png"
        }
        ```
*   **`PUT /api/v1/brands/{id}`**: Edit a Brand (Admin only).
*   **`DELETE /api/v1/brands/{id}`**: Soft delete/Archive a Brand (Admin only).

### 4.4. Content Management
*   **`GET /api/v1/contents`**: Fetch content cards. Supports filters: `brandId`, `stage`, `type`, `assigneeId`.
    *   *Request Parameters*: `?brandId=5&stage=IDEA&page=0&size=10&sort=dueDate,asc`
*   **`GET /api/v1/contents/{id}`**: Get details of a Content card.
*   **`POST /api/v1/contents`**: Create a content card. (Admin / Manager only).
    *   *Request Payload*:
        ```json
        {
          "brandId": 5,
          "title": "Top 10 Ember.js Tricks",
          "description": "Showcasing components & typescript integrations",
          "type": "BLOG",
          "stage": "IDEA",
          "priority": "HIGH",
          "dueDate": "2026-07-05T00:00:00Z"
        }
        ```
*   **`PUT /api/v1/contents/{id}`**: Update content details or advance stages. (Admin / Manager only).
*   **`DELETE /api/v1/contents/{id}`**: Soft delete a content card. (Admin / Manager only).

### 4.5. Research Module
*   **`GET /api/v1/contents/{contentId}/research`**: List research cards for a content card.
*   **`POST /api/v1/contents/{contentId}/research`**: Add research item (Note, Link, or AI response).
    *   *Request Payload*:
        ```json
        {
          "type": "LINK",
          "title": "Competitor Analysis",
          "url": "https://youtube.com/watch?v=123",
          "contentText": "Video pacing and hook structure ideas"
        }
        ```
*   **`GET /api/v1/research/{id}`**: Get details of a single research card.
*   **`PUT /api/v1/research/{id}`**: Update a research card.
*   **`DELETE /api/v1/research/{id}`**: Hard delete a research card.

### 4.6. Script Module
*   **`GET /api/v1/contents/{contentId}/scripts`**: List all script versions for a content card.
*   **`POST /api/v1/contents/{contentId}/scripts`**: Push a new script draft snapshot.
    *   *Request Payload*:
        ```json
        {
          "documentType": "INTERNAL",
          "editorContent": "Welcome to this tutorial on clean code...",
          "externalDocumentUrl": null,
          "uploadedFileReference": null,
          "generatedScript": "Welcome to this tutorial on clean code..."
        }
        ```
*   **`GET /api/v1/scripts/{id}`**: Retrieve script draft details.
*   **`PUT /api/v1/scripts/{id}`**: Update a script version.
*   **`DELETE /api/v1/scripts/{id}`**: Hard delete a script draft.

### 4.7. Assignments & Tasks

#### Assignments
*   **`POST /api/v1/contents/{contentId}/assignments`**: Create an assignment mapping a contributor role to content.
    *   *Request Payload*:
        ```json
        {
          "assignedToUserId": 4,
          "assignmentType": "SCRIPT",
          "notes": "Draft initial script",
          "dueDate": "2026-06-30T12:00:00Z"
        }
        ```
*   **`GET /api/v1/contents/{contentId}/assignments`**: Get all assignments for a content card.
*   **`GET /api/v1/assignments/my`**: Get current contributor's assignments. Optional query: `?status=ASSIGNED`
*   **`GET /api/v1/assignments/{id}`**: Fetch single assignment details.
*   **`PUT /api/v1/assignments/{id}`**: Update assignment user, type, notes, or due date. (Admin/Manager only).
*   **`PATCH /api/v1/assignments/{id}/status`**: Update assignment execution status (e.g. `IN_PROGRESS`, `COMPLETED`, `BLOCKED`).
    *   *Request Payload*:
        ```json
        {
          "status": "COMPLETED"
        }
        ```
*   **`DELETE /api/v1/assignments/{id}`**: Hard delete an assignment. (Admin/Manager only).

#### Tasks (Checklist items under assignments)
*   **`POST /api/v1/assignments/{assignmentId}/tasks`**: Append checklist task.
    *   *Request Payload*:
        ```json
        {
          "title": "Write script intro",
          "description": "Ensure a solid hook",
          "assignedToUserId": 4,
          "priority": "HIGH",
          "dueDate": "2026-06-25T12:00:00Z"
        }
        ```
*   **`GET /api/v1/assignments/{assignmentId}/tasks`**: List tasks under an assignment.
*   **`GET /api/v1/tasks/{id}`**: Get single task details.
*   **`GET /api/v1/tasks/my`**: Get current contributor's checklist tasks. Optional queries: `?status=TODO&priority=HIGH`
*   **`PUT /api/v1/tasks/{id}`**: Update full task details. (Admin/Manager only).
*   **`PATCH /api/v1/tasks/{id}/status`**: Toggle task execution status (`TODO`, `IN_PROGRESS`, `BLOCKED`, `DONE`).
    *   *Request Payload*:
        ```json
        {
          "status": "DONE"
        }
        ```
*   **`DELETE /api/v1/tasks/{id}`**: Hard delete a task. (Admin/Manager only).

### 4.8. Asset Tracking
*   **`POST /api/v1/contents/{contentId}/assets`**: Register a media asset reference.
    *   *Request Payload*:
        ```json
        {
          "name": "Rough Video Edit V1",
          "description": "Initial cuts",
          "assetType": "EDITED_VIDEO",
          "assetSource": "GOOGLE_DRIVE",
          "fileUrl": "https://drive.google.com/file/d/rough_draft",
          "fileSize": 104857600,
          "mimeType": "video/mp4",
          "version": 1
        }
        ```
*   **`GET /api/v1/contents/{contentId}/assets`**: Get all assets associated with content.
*   **`GET /api/v1/assets/{id}`**: Fetch single asset details.
*   **`PUT /api/v1/assets/{id}`**: Update asset metadata.
*   **`DELETE /api/v1/assets/{id}`**: Hard delete an asset reference.

### 4.9. Activity Timeline
*   **`GET /api/v1/contents/{contentId}/activities`**: Fetch content audit logs chronologically (sorted by newest first by default).
*   **`GET /api/v1/activities/{id}`**: Retrieve detailed activity log entry by ID.

### 4.10. Content Calendar Projections
*   **`GET /api/v1/calendar`**: Fetch scheduled content range. Query parameters: `?startDate=2026-06-01T00:00:00Z&endDate=2026-06-30T23:59:59Z`
*   **`GET /api/v1/calendar/upcoming`**: Retrieve paginated upcoming content.
*   **`GET /api/v1/calendar/scheduled`**: Fetch scheduled content cards.
*   **`GET /api/v1/calendar/published`**: Get published content history.
*   **`GET /api/v1/calendar/overdue`**: List overdue content cards.

### 4.11. Analytics Dashboard
*   **`GET /api/v1/analytics/dashboard`**: Return home page operational metrics.
*   **`GET /api/v1/analytics/content`**: Retrieve content counts grouped by stage, type, and priority.
*   **`GET /api/v1/analytics/assignments`**: Get assignments status and type counts.
*   **`GET /api/v1/analytics/tasks`**: Get task statistics and overdue counts.
*   **`GET /api/v1/analytics/publishing`**: Return publication calendar timeline performance trends.

### 4.12. AI Brainstorming & Generation
*   **`POST /api/v1/ai/contents/{contentId}/brainstorm`**: Ask AI Gateway to generate click hooks, title options, and outlining recommendations. Results are saved as an `AI_BRAINSTORM` ResearchItem.
*   **`POST /api/v1/ai/contents/{contentId}/generate-script`**: Prompt AI Gateway to generate a conversation script draft based on compiled research context. Outlines are appended as Script Version 1.0.

---

## 5. Architectural API Operations (System Foundations)

### 5.1. Request Correlation Trace
Every client request passing through the servlet boundaries is evaluated for diagnostic tracking:
*   **Request Header**: `X-Correlation-Id` (UUID text format). If the client supplies a trace ID, the system preserves and propagates it. If omitted, the server dynamically generates a new UUID.
*   **Response Header**: `X-Correlation-Id`. The header is returned to client responses to coordinate tracing reports.
*   **Thread/MDC Propagation**: The correlation ID automatically traverses async process barriers, showing up in logs for background operations.

### 5.2. Local Caching Operations
To optimize query performance for low-volatility entities, the REST layers leverage localized, read-through caching:
*   **Cached Entities**: Organization, Brand, User Profile.
*   **Cache Headers / Refresh**: Write operations (such as updates, additions, and deletions) immediately invalidate cache states, ensuring client reads remain correct. Highly volatile content entities (Content, Task, Assignment, Script, Research, AI, Assets) are explicitly uncached at the server level.

### 5.3. Idempotency Key Control (AI Endpoints Only)
To prevent duplicate content/script creation, AI generation endpoints accept a client-provided idempotency key.
*   **Target Endpoints**:
    *   `POST /api/v1/ai/contents/{contentId}/brainstorm`
    *   `POST /api/v1/ai/contents/{contentId}/generate-script`
*   **Request Header**: `Idempotency-Key` (e.g., `Idempotency-Key: idemp-uuid-12345`).
*   **Responses**:
    *   *First Request*: Processed normally. Returns `200 OK` or `201 Created` with results, which are cached for 24 hours.
    *   *Duplicate Request (Completed)*: Returns the cached response body, headers, and status code directly without re-invoking the AI provider.
    *   *Duplicate Request (In Progress)*: Returns `409 Conflict` with a JSON warning: `{"message":"A request with the same idempotency key is already in progress."}`.
    *   *Failed Request*: If the first request returns a non-2xx error (e.g., 5xx downstream timeout), the entry is removed from the cache to allow the user/client to retry immediately.

### 5.4. Business Metrics & Observability (Actuator)
In addition to JVM and platform stats, the system registers custom operational metric meters.
*   **Metric Retrieval**: `GET /actuator/metrics/{metricName}` (e.g., `GET /actuator/metrics/creatorops.ai.requests`).
*   **Key Custom Metrics**:
    *   `creatorops.ai.requests`: Total attempts to invoke the AI provider.
    *   `creatorops.ai.success`: Successful AI provider executions.
    *   `creatorops.ai.failures`: Failed AI provider executions (mapped to exceptions).
    *   `creatorops.brainstorms.generated`: Total brainstorm outline records successfully saved.
    *   `creatorops.scripts.generated`: Total script draft records successfully saved.
    *   `creatorops.content.created`: Total content cards initialized in the workspace.
    *   `creatorops.assignments.created`: Total assignments created.
    *   `creatorops.tasks.completed`: Total tasks marked completed (status = `DONE`).
    *   `creatorops.ai.circuit.open`: Total transitions of the Resilience4j Circuit Breaker to the OPEN state.
    *   `creatorops.ai.retry.count`: Total Resilience4j retry attempts triggered due to transient failures.
