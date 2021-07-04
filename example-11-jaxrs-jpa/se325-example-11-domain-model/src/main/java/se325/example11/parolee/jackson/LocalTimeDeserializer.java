package se325.example11.parolee.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeDeserializer extends StdDeserializer<LocalTime> {

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public LocalTimeDeserializer() {
        this(null);
    }

    public LocalTimeDeserializer(Class<LocalDate> clazz) {
        super(clazz);
    }

    @Override
    public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return LocalTime.parse(jsonParser.getText(), FORMATTER);
    }
}
