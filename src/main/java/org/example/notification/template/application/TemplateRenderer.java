package org.example.notification.template.application;

import java.util.*;
import java.util.regex.*;
import org.springframework.stereotype.Component;

@Component
public class TemplateRenderer {
  private static final Pattern VARIABLE = Pattern.compile("\\$\\{([A-Za-z][A-Za-z0-9_.-]*)}");

  public String render(String template, Map<String, String> variables) {
    if (template == null) return null;
    Matcher matcher = VARIABLE.matcher(template);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String value = variables.get(matcher.group(1));
      if (value == null)
        throw new IllegalArgumentException("Missing template variable: " + matcher.group(1));
      matcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  public Set<String> variables(String template) {
    if (template == null) return Set.of();
    Set<String> result = new LinkedHashSet<>();
    Matcher matcher = VARIABLE.matcher(template);
    while (matcher.find()) result.add(matcher.group(1));
    return result;
  }
}
