package org.example.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.example.notification.template.application.TemplateRenderer;
import org.junit.jupiter.api.Test;

class TemplateRendererTest {
  private final TemplateRenderer renderer = new TemplateRenderer();

  @Test
  void replacesVariablesWithoutTreatingReplacementAsRegex() {
    assertEquals("Hello $Ada", renderer.render("Hello ${name}", Map.of("name", "$Ada")));
  }

  @Test
  void rejectsUnresolvedVariables() {
    assertThrows(IllegalArgumentException.class, () -> renderer.render("Hello ${name}", Map.of()));
  }
}
