package org.sundbybergheat.baseballstreaming.models;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import io.vavr.control.Either;
import java.io.IOException;

/**
 * <code>EitherModule</code> is an extension of <code>
 * com.fasterxml.jackson.databind.module.SimpleModule</code>. The purpose of this module is to
 * enable proper (de)serialization of the <code>io.vavr.control.Either</code> type from/to JSON.
 * Example use cases: The JSON source provides (for the same type) <code>{"field": 1234}</code> as
 * well as <code>{"field": "some string"}</code> At a first glanse this seems like an inconsistency
 * from the JSON source but it is in fact perfectly valid and can be (de)serialized in a type
 * correct and safe manner using the <code>io.vavr.control.Either</code> type and this <code>
 * EitherModule</code>.
 *
 * <p>Example:
 *
 * <blockquote>
 *
 * <pre>
 *
 * record MyRecord(Either&lt;Integer, String&gt; field) {}
 *
 * ObjectMapper mapper = new ObjectMapper(new JsonFactory())
 *     .registerModule(new EitherModule());
 *
 * MyRecord my1 = mapper.readValue("{\"field\": 1234}", MyRecord.class);
 *
 * // my1 == MyRecord[field=Left(1234)]
 *
 * MyRecord my2 = mapper.readValue("{\"field\": \"some string\"}", MyRecord.class);
 *
 * // my2 == MyRecord[field=Right(some string)]
 *
 * String json = mapper.writeValueAsString(new EitherTestModel(Either.left(1234)));
 *
 * // json == "{\"field\":1234}"
 *
 * </pre>
 *
 * </blockquote>
 *
 * <p>
 *
 * @author Martin Kero
 */
public class EitherModule extends SimpleModule {

  private static class EitherDeserializer extends JsonDeserializer<Either<?, ?>>
      implements ContextualDeserializer {

    private JavaType left;
    private JavaType right;

    public EitherDeserializer() {}

    private EitherDeserializer(final JavaType left, final JavaType right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public JsonDeserializer<Either<?, ?>> createContextual(
        DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
      JavaType leftType = property.getType().containedType(0);
      JavaType rightType = property.getType().containedType(1);
      return new EitherDeserializer(leftType, rightType);
    }

    @Override
    public Either<?, ?> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JacksonException {
      try {
        return Either.left(ctxt.readValue(p, left));
      } catch (Exception e1) {
        try {
          return Either.right(ctxt.readValue(p, right));
        } catch (Exception e2) {
          throw new JsonParseException(
              p,
              "Unable to deserialize Either<%s,%s> from token %s"
                  .formatted(
                      left.getRawClass().getSimpleName(),
                      right.getRawClass().getSimpleName(),
                      p.getCurrentToken()));
        }
      }
    }
  }

  private static class EitherConverter extends StdConverter<Either<?, ?>, Object> {
    @Override
    public Object convert(Either<?, ?> value) {
      if (value.isLeft()) {
        return value.getLeft();
      } else if (value.isRight()) {
        return value.get();
      } else {
        return null;
      }
    }
  }

  public EitherModule() {
    super();
    this.addDeserializer(Either.class, new EitherDeserializer())
        .addSerializer(Either.class, new StdDelegatingSerializer(new EitherConverter()));
  }
}
