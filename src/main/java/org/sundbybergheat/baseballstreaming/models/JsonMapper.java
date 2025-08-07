package org.sundbybergheat.baseballstreaming.models;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;

public class JsonMapper {
  private static final ObjectMapper JSON =
      new ObjectMapper(new JsonFactory())
          .registerModule(new Jdk8Module())
          .registerModule(new EitherModule())
          .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
          .setSerializationInclusion(Include.NON_NULL)
          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
          .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

  public static <T> T fromJson(final String value, Class<T> clazz) throws IOException {
    return JSON.readValue(value, clazz);
  }

  public static <T> T fromJson(final String value, final TypeReference<T> typeReference)
      throws IOException {
    return JSON.readValue(value, typeReference);
  }

  public static String toJson(final Object value) throws IllegalArgumentException {
    try {
      return JSON.writeValueAsString(value).trim();
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Error serializing object: %s", value), e);
    }
  }
}
