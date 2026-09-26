package org.example.notification.delivery.api.dto;

import jakarta.validation.constraints.*;
import java.util.Map;

public class RecipientRequest {
  @NotBlank
  @Size(max = 500)
  private String address;

  private Map<String, String> variables;

  public RecipientRequest() {}

  public String getAddress() {
    return address;
  }

  public void setAddress(String v) {
    address = v;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> v) {
    variables = v;
  }
}
