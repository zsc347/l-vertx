package com.scaiz.eventbus;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.scaiz.json.JsonObject;
import com.scaiz.support.CaseInsensitiveHeaders;
import com.scaiz.support.MultiMap;

public class DeliveryOptions {

  public static final long DEFAULT_TIMEOUT = 30 * 1000;

  private long timeout = DEFAULT_TIMEOUT;
  private String codecName;
  private MultiMap headers;


  public DeliveryOptions() {
  }

  public DeliveryOptions(JsonObject json) {
    this.timeout = Optional.ofNullable(json.getLong("timeout")).orElse(DEFAULT_TIMEOUT);
    this.codecName = json.getString("codecName");
    JsonObject jsonHeaders = json.getJsonObject("headers");
    if (jsonHeaders != null) {
      headers = new CaseInsensitiveHeaders();
      for (Map.Entry<String, Object> entry : jsonHeaders) {
        if (!(entry.getValue() instanceof String)) {
          throw new IllegalStateException("Invalid type for message header value " + entry.getValue().getClass());
        }
        headers.set(entry.getKey(), (String) entry.getValue());
      }
    }
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("timeout", this.timeout);

    if (this.codecName != null) {
      json.put("codecName", this.codecName);
    }

    if (this.headers != null) {
      JsonObject jsonHeaders = new JsonObject();
      headers.entries().forEach(entry -> jsonHeaders.put(entry.getKey(), entry.getValue()));
      json.put("headers", jsonHeaders);
    }
    return json;
  }

  public long getSendTimeout() {
    return timeout;
  }

  public DeliveryOptions setSendTimeout(long timeout) {
    if (timeout < 1) {
      throw new IllegalArgumentException("time out must be >= 1");
    }
    this.timeout = timeout;
    return this;
  }

  public String getCodecName() {
    return codecName;
  }

  public DeliveryOptions setCodecName(String codecName) {
    this.codecName = codecName;
    return this;
  }


  public DeliveryOptions addHeader(String key, String value) {
    ensureHeaders();
    Objects.requireNonNull(key, "no null key accepted");
    Objects.requireNonNull(value, "no null value accepted");
    headers.add(key, value);
    return this;
  }

  public DeliveryOptions setHeaders(MultiMap headers) {
    this.headers = headers;
    return this;
  }

  private void ensureHeaders() {
    if (headers == null) {
      headers = new CaseInsensitiveHeaders();
    }
  }
}
