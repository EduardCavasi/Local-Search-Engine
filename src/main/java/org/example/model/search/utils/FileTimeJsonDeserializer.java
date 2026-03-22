package org.example.model.search.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileTimeJsonDeserializer extends JsonDeserializer<FileTime> {
    @Override
    public FileTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_STRING) {
            return FileTime.from(Instant.parse(p.getText()));
        }
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return FileTime.fromMillis(p.getLongValue());
        }
        return (FileTime) ctxt.handleUnexpectedToken(FileTime.class, p);
    }
}