package com.scaiz.vertx.eventbus;

import com.scaiz.vertx.json.JsonObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DeliveryOptionsTest {

  @Test
  public void testJson() {
    DeliveryOptions defaultOptions = new DeliveryOptions();
    assertEquals(defaultOptions.toJson(), new JsonObject().put("timeout", DeliveryOptions.DEFAULT_TIMEOUT));

    JsonObject fullJson = new JsonObject()
      .put("timeout", 15000)
      .put("codecName", "codec")
      .put("headers", new JsonObject().put("header", "headerContent"));

    DeliveryOptions configJson = new DeliveryOptions()
      .setSendTimeout(15000)
      .setCodecName("codec")
      .addHeader("header", "headerContent");
    assertEquals(fullJson, configJson.toJson());
    assertEquals(new DeliveryOptions(fullJson).toJson(), configJson.toJson());
  }
}
