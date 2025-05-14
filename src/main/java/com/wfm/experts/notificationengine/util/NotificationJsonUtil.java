package com.wfm.experts.notificationengine.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // For Java 8 Date/Time types
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for JSON serialization and deserialization using Jackson.
 * This provides a centralized ObjectMapper configuration for consistent JSON processing
 * throughout the notification engine where manual JSON handling is required.
 */
public final class NotificationJsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(NotificationJsonUtil.class);
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // Configure the ObjectMapper as needed
        objectMapper.registerModule(new JavaTimeModule()); // Support for Java 8 Date/Time (LocalDate, LocalDateTime, etc.)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Write dates in ISO-8601 format
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // Ignore unknown properties during deserialization
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty printing: enable for dev/debug, disable for prod performance
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private NotificationJsonUtil() {
    }

    /**
     * Serializes an object to its JSON string representation.
     *
     * @param object The object to serialize.
     * @return An {@link Optional} containing the JSON string if serialization is successful,
     * or an empty Optional if the input object is null or serialization fails.
     */
    public static Optional<String> toJson(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing object to JSON. Object class: {}", object.getClass().getName(), e);
            return Optional.empty();
        }
    }

    /**
     * Serializes an object to its JSON string representation with pretty printing.
     * Useful for logging or debugging.
     *
     * @param object The object to serialize.
     * @return An {@link Optional} containing the pretty-printed JSON string if serialization is successful,
     * or an empty Optional if the input object is null or serialization fails.
     */
    public static Optional<String> toJsonPretty(Object object) {
        if (object == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing object to pretty JSON. Object class: {}", object.getClass().getName(), e);
            return Optional.empty();
        }
    }


    /**
     * Deserializes a JSON string to an object of the specified class.
     *
     * @param json  The JSON string to deserialize.
     * @param clazz The class of the target object.
     * @param <T>   The type of the target object.
     * @return An {@link Optional} containing the deserialized object if successful,
     * or an empty Optional if the JSON string is null/empty or deserialization fails.
     */
    public static <T> Optional<T> fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (IOException e) {
            logger.error("Error deserializing JSON string to object of class {}. JSON: {}", clazz.getSimpleName(), json, e);
            return Optional.empty();
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified generic type reference.
     * Useful for deserializing to generic collections like List<MyObject> or Map<String, MyObject>.
     *
     * @param json          The JSON string to deserialize.
     * @param typeReference The TypeReference representing the target generic type.
     * @param <T>           The type of the target object.
     * @return An {@link Optional} containing the deserialized object if successful,
     * or an empty Optional if the JSON string is null/empty or deserialization fails.
     */
    public static <T> Optional<T> fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (IOException e) {
            logger.error("Error deserializing JSON string to generic type {}. JSON: {}", typeReference.getType(), json, e);
            return Optional.empty();
        }
    }

    /**
     * Deserializes a JSON string to a Map<String, Object>.
     *
     * @param json The JSON string to deserialize.
     * @return An {@link Optional} containing the deserialized map if successful,
     * or an empty Optional if the JSON string is null/empty or deserialization fails.
     */
    public static Optional<Map<String, Object>> fromJsonToMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            return Optional.of(objectMapper.readValue(json, typeRef));
        } catch (IOException e) {
            logger.error("Error deserializing JSON string to Map<String, Object>. JSON: {}", json, e);
            return Optional.empty();
        }
    }

    /**
     * Provides access to the configured ObjectMapper instance if needed for more complex scenarios.
     * Use with caution, prefer using the utility methods.
     *
     * @return The configured ObjectMapper.
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
