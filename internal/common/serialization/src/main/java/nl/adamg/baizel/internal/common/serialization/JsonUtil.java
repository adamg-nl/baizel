package nl.adamg.baizel.internal.common.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper MAPPER = JsonMapper
            .builder(JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build())
            .addModule(new ParameterNamesModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .constructorDetector(ConstructorDetector.USE_PROPERTIES_BASED)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    /// @throws JsonException if JSON processing fails
    public static String toJson(Object object, boolean pretty) throws JsonException {
        try {
            return (pretty ? MAPPER.writerWithDefaultPrettyPrinter() : MAPPER.writer()).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /// @throws JsonException if JSON processing fails
    public static Map<String, Object> fromJson(String json) throws JsonException {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /// @throws JsonException if JSON processing fails
    public static <T> T fromJson(String json, Class<T> type) throws JsonException {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /// @throws JsonException if JSON processing fails
    public static Map<String, Object> read(Path jsonFilePath) throws IOException, JsonException {
        return fromJson(Files.readString(jsonFilePath));
    }

    /// @throws JsonException if JSON processing fails
    public static <T> T read(Path jsonFilePath, Class<T> type) throws IOException, JsonException {
        return fromJson(Files.readString(jsonFilePath), type);
    }

    /// @throws JsonException if JSON processing fails
    public static <T> void write(Path jsonFilePath, T object) throws IOException, JsonException {
        Files.writeString(jsonFilePath, toJson(object, true));
    }

    public static class JsonException extends RuntimeException {
        public JsonException(JsonProcessingException cause) {
            super(cause);
        }
    }
}
