package com.wzp.cloud.graphql;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

import static graphql.scalars.util.Kit.typeName;

public class LocalDateTimeScalar extends GraphQLScalarType {

    @SuppressWarnings("deprecation")
    public LocalDateTimeScalar() {
        super("LocalDateTime", "An RFC-3339 compliant DateTime Scalar", new Coercing<LocalDateTime, String>() {
            @Override
            public String serialize(Object input) throws CoercingSerializeException {
                LocalDateTime localDateTime;
                if (input instanceof LocalDateTime) {
                    localDateTime = (LocalDateTime) input;
                } else if (input instanceof String) {
                    localDateTime = parseLocalDateTime(input.toString(), CoercingSerializeException::new);
                } else {
                    throw new CoercingSerializeException(
                            "Expected something we can convert to 'java.time.LocalDateTime' but was '" + typeName(input) + "'."
                    );
                }
                try {
                    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
                } catch (DateTimeException e) {
                    throw new CoercingSerializeException(
                            "Unable to turn TemporalAccessor into LocalDateTime because of : '" + e.getMessage() + "'."
                    );
                }
            }

            @Override
            public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                LocalDateTime localDateTime;
                if (input instanceof LocalDateTime) {
                    localDateTime = (LocalDateTime) input;
                } else if (input instanceof String) {
                    localDateTime = parseLocalDateTime(input.toString(), CoercingParseValueException::new);
                } else {
                    throw new CoercingParseValueException(
                            "Expected a 'String' but was '" + typeName(input) + "'."
                    );
                }
                return localDateTime;
            }

            @Override
            public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                if (!(input instanceof StringValue)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    );
                }
                return parseLocalDateTime(((StringValue) input).getValue(), CoercingParseLiteralException::new);
            }

            private LocalDateTime parseLocalDateTime(String s, Function<String, RuntimeException> exceptionMaker) {
                try {
                    return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    throw exceptionMaker.apply("Invalid RFC3339 value : '" + s + "'. because of : '" + e.getMessage() + "'");
                }
            }
        });
    }
}
