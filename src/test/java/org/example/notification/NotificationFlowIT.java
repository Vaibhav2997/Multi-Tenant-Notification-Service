package org.example.notification;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationFlowIT {
  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void malformedJsonUsesProblemDetailsContract() throws Exception {
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
        .andExpect(jsonPath("$.detail").value("Request body contains malformed JSON"));
  }

  @Test
  void tenantAdminCanConfigureTemplateAndSubmitAnIdempotentBatch() throws Exception {
    mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    String platformToken = login("platform@example.com", "test-password");
    JsonNode tenant =
        json(
            mvc.perform(
                    post("/api/v1/platform/tenants")
                        .header("Authorization", bearer(platformToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme\",\"slug\":\"acme\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String tenantId = tenant.get("id").asText();

    mvc.perform(
            post("/api/v1/platform/tenants/{tenantId}/admins", tenantId)
                .header("Authorization", bearer(platformToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@acme.test\",\"password\":\"long-password\"}"))
        .andExpect(status().isOk());
    String tenantToken = login("admin@acme.test", "long-password");

    mvc.perform(get("/api/v1/platform/tenants").header("Authorization", bearer(tenantToken)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mvc.perform(
            put("/api/v1/tenant/channels/EMAIL")
                .header("Authorization", bearer(tenantToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":true,\"senderIdentity\":\"noreply@acme.test\"}"))
        .andExpect(status().isOk());
    JsonNode template =
        json(
            mvc.perform(
                    post("/api/v1/tenant/templates")
                        .header("Authorization", bearer(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            "{\"name\":\"welcome\",\"channel\":\"EMAIL\",\"subjectTemplate\":\"Hi ${name}\",\"bodyTemplate\":\"Welcome ${name}\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

    String request =
        "{\"templateId\":\""
            + template.get("id").asText()
            + "\",\"recipients\":[{\"address\":\"person@example.test\",\"variables\":{\"name\":\"Ada\"}}]}";
    String first =
        mvc.perform(
                post("/api/v1/tenant/notification-batches")
                    .header("Authorization", bearer(tenantToken))
                    .header("Idempotency-Key", "welcome-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.totalDeliveries").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();

    mvc.perform(
            post("/api/v1/tenant/notification-batches")
                .header("Authorization", bearer(tenantToken))
                .header("Idempotency-Key", "welcome-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(json(first).get("id").asText()));

    String conflictingRequest = request.replace("person@example.test", "different@example.test");
    mvc.perform(
            post("/api/v1/tenant/notification-batches")
                .header("Authorization", bearer(tenantToken))
                .header("Idempotency-Key", "welcome-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingRequest))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
    mvc.perform(get("/api/v1/tenant/deliveries").header("Authorization", bearer(tenantToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  private String login(String email, String password) throws Exception {
    String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
    String response =
        mvc.perform(
                post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json(response).get("accessToken").asText();
  }

  private JsonNode json(String value) throws Exception {
    return objectMapper.readTree(value);
  }

  private String bearer(String token) {
    return "Bearer " + token;
  }
}
