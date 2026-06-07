# API Design Specification

This document details the REST API design conventions, authorization integration, response structures, and a comprehensive endpoint catalog for the **CreatorOps** platform.

---

## 1. REST Conventions & Standards

The CreatorOps API is built following standard RESTful design practices:

1.  **Transport Protocol**: All API communication must be encrypted over HTTPS.
2.  **Resource Naming**: Plural nouns for resource identifiers (e.g., `/api/brands`, `/api/contents`).
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
*   **Token Retrieval**: Clients request tokens by providing email credentials to the public login endpoint `/api/auth/login`.
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
      "instance": "/api/contents",
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
      "instance": "/api/organizations/1/brands"
    }
    ```

---

## 4. Endpoint Catalog

### 4.1. Auth Endpoint (Public)
*   **`POST /api/auth/login`**: Validate credentials and fetch a JWT token.
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
*   **`POST /api/organizations`**: Register a new Organization.
    *   *Request Payload*:
        ```json
        {
          "name": "SLAY Media Group",
          "logoUrl": "https://example.com/logos/slay-media.png"
        }
        ```
*   **`PUT /api/organizations/{id}`**: Edit details of an Organization.
*   **`DELETE /api/organizations/{id}`**: Soft delete an Organization.

### 4.3. Brand Management
*   **`GET /api/brands`**: Fetch all active brands under the user's Organization.
*   **`POST /api/brands`**: Create a new Brand (Admin only).
    *   *Request Payload*:
        ```json
        {
          "name": "SLAY Fashion",
          "description": "Fashion guides, hauls, and styling tips",
          "logoUrl": "https://example.com/logos/slay-fashion.png"
        }
        ```
*   **`PUT /api/brands/{id}`**: Edit a Brand (Admin only).
*   **`DELETE /api/brands/{id}`**: Soft delete/Archive a Brand (Admin only).

### 4.4. Content Management
*   **`GET /api/contents`**: Fetch content cards. Supports filters: `brandId`, `stage`, `type`, `assigneeId`.
    *   *Request Parameters*: `?brandId=5&stage=IDEA&page=0&size=10&sort=dueDate,asc`
*   **`GET /api/contents/{id}`**: Get details of a Content card.
*   **`POST /api/contents`**: Create a content card. (Admin / Manager only).
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
*   **`PUT /api/contents/{id}`**: Update content details or advance stages. (Admin / Manager only).
*   **`DELETE /api/contents/{id}`**: Soft delete a content card. (Admin / Manager only).

### 4.5. Research Module
*   **`GET /api/contents/{contentId}/research-items`**: List research cards.
*   **`POST /api/contents/{contentId}/research-items`**: Add research (Note, Link, or AI response).
    *   *Request Payload*:
        ```json
        {
          "type": "LINK",
          "url": "https://youtube.com/watch?v=123",
          "content": "Competitor layout references"
        }
        ```
*   **`DELETE /api/research-items/{id}`**: Hard delete research cards.

### 4.6. Script Module & AI Actions
*   **`GET /api/contents/{contentId}/script`**: Retrieve current script metadata and contents.
*   **`POST /api/contents/{contentId}/script/versions`**: Push a new manual edit version snapshot.
*   **`POST /api/contents/{contentId}/script/ai-generate`**: Ask AI Gateway to generate a script draft.
    *   *Request Payload*:
        ```json
        {
          "promptInstructions": "Write a funny intro, focused on beginner coders."
        }
        ```
*   **`POST /api/contents/{contentId}/script/ai-hooks`**: Ask AI Gateway to return hook variants.

### 4.7. Assignments & Tasks
*   **`POST /api/contents/{contentId}/assignments`**: Create assignments mapping roles to contributors.
    *   *Request Payload*:
        ```json
        {
          "userId": 4,
          "role": "Script Writing",
          "status": "PENDING"
        }
        ```
*   **`PUT /api/assignments/{id}`**: Update assignment progress status (e.g. to `IN_PROGRESS`).
*   **`POST /api/contents/{contentId}/tasks`**: Append checklist tasks.
*   **`PUT /api/tasks/{id}`**: Toggle completion state of a task.
*   **`DELETE /api/tasks/{id}`**: Hard delete checklist tasks.

### 4.8. Activity Timeline
*   **`GET /api/contents/{contentId}/activity-logs`**: Fetch changes chronologically.
