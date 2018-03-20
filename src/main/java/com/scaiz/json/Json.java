package com.scaiz.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Json {

  public static ObjectMapper mapper = new ObjectMapper();
  public static ObjectMapper prettyMapper = new ObjectMapper();

  static {
    prettyMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    module.addSerializer(JsonArray.class, new JsonArraySerializer());

    mapper.registerModule(module);
    prettyMapper.registerModule(module);
  }

  public static String encode(Object obj) throws EncodeException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new EncodeException("Failed encode to json" + e.getMessage());
    }
  }

  public static String encodePretty(Object obj) throws EncodeException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new EncodeException("Failed encode to json" + e.getMessage());
    }
  }

  public static <T> T decode(String str, Class<T> clazz)
      throws DecodeException {
    try {
      return mapper.readValue(str, clazz);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode " + e.getMessage());
    }
  }

  public static <T> T decode(String str, TypeReference<T> type)
      throws DecodeException {
    try {
      return mapper.readValue(str, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode " + e.getMessage());
    }
  }

  private static class JsonObjectSerializer extends JsonSerializer<JsonObject> {

    @Override
    public void serialize(JsonObject value, JsonGenerator jsonGen,
        SerializerProvider provider) throws IOException {
      jsonGen.writeObject(value.getMap());
    }
  }


  private static class JsonArraySerializer extends JsonSerializer<JsonArray> {

    @Override
    public void serialize(JsonArray value, JsonGenerator jgen,
        SerializerProvider provider) throws IOException {
      jgen.writeObject(value.getList());
    }
  }

  static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
    Iterable<T> iterable = () -> sourceIterator;
    return StreamSupport.stream(iterable.spliterator(), false);
  }

}
