package se325.example11.parolee.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeSerializer extends StdSerializer<LocalTime> {

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public LocalTimeSerializer() {
        this(null);
    }

    public LocalTimeSerializer(Class<LocalTime> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(LocalTime localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(localDate.format(FORMATTER));
    }
}
