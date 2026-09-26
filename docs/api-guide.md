# API Guide

Swagger UI is available at `/swagger-ui.html`; the machine-readable specification is available at `/v3/api-docs` and `/v3/api-docs.yaml`.

## Authentication

```http
POST /api/v1/auth/login
Content-Type: application/json

{"email":"platform@example.com","password":"change-me-now"}
```

Pass the returned token as `Authorization: Bearer <accessToken>`.

The token identifies the administrator, role, and, when applicable, tenant. Tenant endpoints derive ownership from the authenticated principal rather than trusting a caller-supplied tenant identifier. Platform endpoints are separately role-gated.

## Core workflow

1. Platform admin creates a tenant and tenant administrator.
2. Tenant administrator enables a channel.
3. Tenant administrator creates an active template.
4. Tenant administrator submits a batch with `Idempotency-Key`.
5. Tenant administrator queries the batch or delivery report.

Example notification submission:

```http
POST /api/v1/tenant/notification-batches
Authorization: Bearer <tenant-token>
Idempotency-Key: welcome-2026-001
Content-Type: application/json

{
  "templateId": "<template-uuid>",
  "scheduledAt": null,
  "variables": {"company": "Acme"},
  "recipients": [
    {
      "address": "ada@example.test",
      "variables": {"name": "Ada"}
    }
  ]
}
```

Recipient variables override batch variables. Missing `${variable}` values reject the entire submission.

## Endpoint summary

- Authentication: `POST /api/v1/auth/login`
- Tenants: `POST|GET /api/v1/platform/tenants`
- Tenant admins: `POST /api/v1/platform/tenants/{tenantId}/admins`
- Platform policy: `GET|PUT /api/v1/platform/policy`
- Tenant rate override: `GET|PUT /api/v1/platform/tenants/{tenantId}/rate-limit`
- Channels: `GET /api/v1/tenant/channels`, `PUT /api/v1/tenant/channels/{channel}`
- Templates: `POST|GET /api/v1/tenant/templates`, `GET|PUT /api/v1/tenant/templates/{id}`
- Batches: `POST /api/v1/tenant/notification-batches`, `GET /api/v1/tenant/notification-batches/{id}`
- Reports: `GET /api/v1/tenant/deliveries`, `GET /api/v1/tenant/deliveries/{id}`

Errors use RFC Problem Details and include a stable `code` property.

## API design rationale

| Area | API decision | Why it matters |
| --- | --- | --- |
| Platform administration | Separate platform endpoints require PLATFORM_ADMIN. | Tenant administrators cannot create tenants or change global policy. |
| Tenant operations | Tenant paths do not carry a tenant identifier. | The authenticated tenant is the ownership boundary and cross-tenant probing is reduced. |
| Batch submission | An Idempotency-Key request header is required. | Client retries remain safe and isolated per tenant. |
| Scheduling | scheduledAt is an ISO-8601 UTC Instant; null means immediate. | One absolute time avoids time-zone ambiguity. |
| Reporting | Delivery list has status, channel, batch, time filters, and pagination. | Queries remain tenant-scoped and practical as history grows. |
| Errors | RFC Problem Details with stable codes. | Consumers can handle validation, conflict, and not-found outcomes predictably. |

### Contract rules

- Recipient variables override batch variables; unresolved variables reject the whole batch.
- The same key and canonical request return the original batch; the same key with different content returns 409 Conflict.
- Tenant-owned resources are resolved through the caller tenant. A resource from another tenant returns 404 Not Found.
- A batch response is an aggregate. Delivery detail exposes the rendered snapshot, provider attempts, and state-transition audit trail.
