# API Guide

Swagger UI is available at `/swagger-ui.html`; the machine-readable specification is available at `/v3/api-docs` and `/v3/api-docs.yaml`.

## Authentication

```http
POST /api/v1/auth/login
Content-Type: application/json

{"email":"platform@example.com","password":"change-me-now"}
```

Pass the returned token as `Authorization: Bearer <accessToken>`.

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
