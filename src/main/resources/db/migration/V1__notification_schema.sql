create table tenants (
  id uuid primary key, name varchar(120) not null, slug varchar(80) not null unique,
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null
);
create table app_users (
  id uuid primary key, tenant_id uuid references tenants(id), email varchar(180) not null unique,
  password_hash varchar(255) not null, role varchar(30) not null, active boolean not null,
  created_at timestamp with time zone not null
);
create table platform_policy (
  id bigint primary key, default_rate_per_minute integer not null, max_batch_size integer not null,
  max_attempts integer not null, base_retry_delay_seconds integer not null,
  max_retry_delay_seconds integer not null, updated_at timestamp with time zone not null
);
create table tenant_rate_limits (
  id uuid primary key, tenant_id uuid not null unique references tenants(id),
  requests_per_minute integer not null, updated_at timestamp with time zone not null
);
create table channel_configurations (
  id uuid primary key, tenant_id uuid not null references tenants(id), channel varchar(20) not null,
  enabled boolean not null, sender_identity varchar(180), updated_at timestamp with time zone not null,
  unique (tenant_id, channel)
);
create table templates (
  id uuid primary key, tenant_id uuid not null references tenants(id), name varchar(120) not null,
  channel varchar(20) not null, subject_template varchar(500), body_template text not null,
  active boolean not null, created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null, unique (tenant_id, name, channel)
);
create table notification_batches (
  id uuid primary key, tenant_id uuid not null references tenants(id),
  template_id uuid not null references templates(id), requested_by uuid not null references app_users(id),
  idempotency_key varchar(160) not null, request_hash varchar(64) not null,
  scheduled_at timestamp with time zone, status varchar(30) not null,
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null,
  unique (tenant_id, idempotency_key)
);
create table deliveries (
  id uuid primary key, tenant_id uuid not null references tenants(id),
  batch_id uuid not null references notification_batches(id), channel varchar(20) not null,
  recipient varchar(500) not null, rendered_subject varchar(500), rendered_body text not null,
  status varchar(30) not null, attempt_count integer not null,
  next_attempt_at timestamp with time zone not null,
  provider_idempotency_key varchar(80) not null unique,
  created_at timestamp with time zone not null, updated_at timestamp with time zone not null,
  completed_at timestamp with time zone
);
create index idx_deliveries_due on deliveries(status, next_attempt_at);
create index idx_deliveries_tenant_created on deliveries(tenant_id, created_at desc);
create index idx_deliveries_tenant_status on deliveries(tenant_id, status, created_at desc);
create index idx_deliveries_batch on deliveries(batch_id);
create table delivery_attempts (
  id uuid primary key, delivery_id uuid not null references deliveries(id), attempt_number integer not null,
  outcome varchar(30) not null, provider_reference varchar(180), error_code varchar(80),
  error_message varchar(1000), started_at timestamp with time zone not null,
  completed_at timestamp with time zone, unique (delivery_id, attempt_number)
);
create table delivery_events (
  id uuid primary key, delivery_id uuid not null references deliveries(id),
  from_status varchar(30), to_status varchar(30) not null, reason varchar(500) not null,
  occurred_at timestamp with time zone not null
);
create table provider_receipts (
  id uuid primary key, provider_idempotency_key varchar(80) not null unique,
  channel varchar(20) not null, provider_reference varchar(180) not null,
  accepted_at timestamp with time zone not null
);
