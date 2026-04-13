package com.nexora.core.graphql;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

@Configuration
public class GraphQlScalarConfig {

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer() {
        GraphQLScalarType dateTimeScalar = GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("ISO-8601 OffsetDateTime")
                .coercing(new Coercing<OffsetDateTime, String>() {
                    @Override
                    public String serialize(Object input) throws CoercingSerializeException {
                        OffsetDateTime value = toOffsetDateTime(input, "serialize");
                        return value.toString();
                    }

                    @Override
                    public OffsetDateTime parseValue(Object input) throws CoercingParseValueException {
                        return toOffsetDateTime(input, "parseValue");
                    }

                    @Override
                    public OffsetDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (!(input instanceof StringValue stringValue)) {
                            throw new CoercingParseLiteralException("DateTime literal must be a string");
                        }
                        try {
                            return OffsetDateTime.parse(stringValue.getValue());
                        } catch (DateTimeParseException ex) {
                            throw new CoercingParseLiteralException("Invalid DateTime format", ex);
                        }
                    }

                    private OffsetDateTime toOffsetDateTime(Object input, String operation) {
                        if (input instanceof OffsetDateTime offsetDateTime) {
                            return offsetDateTime;
                        }
                        if (input instanceof String text) {
                            try {
                                return OffsetDateTime.parse(text);
                            } catch (DateTimeParseException ex) {
                                throw operation.equals("serialize")
                                        ? new CoercingSerializeException("Invalid DateTime format", ex)
                                        : new CoercingParseValueException("Invalid DateTime format", ex);
                            }
                        }
                        throw operation.equals("serialize")
                                ? new CoercingSerializeException("DateTime must be OffsetDateTime or ISO-8601 string")
                                : new CoercingParseValueException("DateTime must be OffsetDateTime or ISO-8601 string");
                    }
                })
                .build();

        return builder -> builder.scalar(dateTimeScalar);
    }
}
