package se325.example12.parolee.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public LocalDateDeserializer() {
        this(null);
    }

    public LocalDateDeserializer(Class<LocalDate> clazz) {
        super(clazz);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return LocalDate.parse(jsonParser.getText(), FORMATTER);
    }
}
