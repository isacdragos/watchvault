package com.watchlist.backend.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtils {
    private JsonUtils() {
    }

    public static Object parse(String source) {
        Parser parser = new Parser(source);
        Object value = parser.parseValue();
        parser.skipWhitespace();

        if (!parser.isAtEnd()) {
            throw new IllegalArgumentException("Unexpected trailing content.");
        }

        return value;
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String stringValue) {
            return "\"" + escape(stringValue) + "\"";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Iterator<? extends Map.Entry<?, ?>> iterator = mapValue.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                builder.append(stringify(String.valueOf(entry.getKey())));
                builder.append(":");
                builder.append(stringify(entry.getValue()));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }

            builder.append("}");
            return builder.toString();
        }

        if (value instanceof Iterable<?> iterableValue) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            Iterator<?> iterator = iterableValue.iterator();

            while (iterator.hasNext()) {
                builder.append(stringify(iterator.next()));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }

            builder.append("]");
            return builder.toString();
        }

        throw new IllegalArgumentException("Unsupported JSON type: " + value.getClass().getName());
    }

    private static String escape(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String source;
        private int index;

        private Parser(String source) {
            this.source = source;
        }

        private Object parseValue() {
            skipWhitespace();

            if (isAtEnd()) {
                throw new IllegalArgumentException("Unexpected end of input.");
            }

            char current = source.charAt(index);

            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> {
                    if (current == '-' || Character.isDigit(current)) {
                        yield parseNumber();
                    }

                    throw new IllegalArgumentException("Unexpected character: " + current);
                }
            };
        }

        private Map<String, Object> parseObject() {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();

            if (peek('}')) {
                expect('}');
                return result;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                result.put(key, value);
                skipWhitespace();

                if (peek('}')) {
                    expect('}');
                    return result;
                }

                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> result = new ArrayList<>();
            expect('[');
            skipWhitespace();

            if (peek(']')) {
                expect(']');
                return result;
            }

            while (true) {
                result.add(parseValue());
                skipWhitespace();

                if (peek(']')) {
                    expect(']');
                    return result;
                }

                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();

            while (!isAtEnd()) {
                char current = source.charAt(index++);

                if (current == '"') {
                    return builder.toString();
                }

                if (current == '\\') {
                    if (isAtEnd()) {
                        throw new IllegalArgumentException("Unterminated escape sequence.");
                    }

                    char escaped = source.charAt(index++);

                    switch (escaped) {
                        case '"', '\\', '/' -> builder.append(escaped);
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> {
                            if (index + 4 > source.length()) {
                                throw new IllegalArgumentException("Invalid unicode escape.");
                            }

                            String hex = source.substring(index, index + 4);
                            builder.append((char) Integer.parseInt(hex, 16));
                            index += 4;
                        }
                        default -> throw new IllegalArgumentException("Invalid escape character.");
                    }

                    continue;
                }

                builder.append(current);
            }

            throw new IllegalArgumentException("Unterminated string.");
        }

        private Object parseNumber() {
            int start = index;

            if (source.charAt(index) == '-') {
                index += 1;
            }

            readDigits();

            boolean decimal = false;

            if (!isAtEnd() && source.charAt(index) == '.') {
                decimal = true;
                index += 1;
                readDigits();
            }

            if (!isAtEnd() && (source.charAt(index) == 'e' || source.charAt(index) == 'E')) {
                decimal = true;
                index += 1;

                if (!isAtEnd() && (source.charAt(index) == '+' || source.charAt(index) == '-')) {
                    index += 1;
                }

                readDigits();
            }

            String literal = source.substring(start, index);
            return decimal ? Double.parseDouble(literal) : Long.parseLong(literal);
        }

        private Object parseLiteral(String literal, Object value) {
            if (!source.startsWith(literal, index)) {
                throw new IllegalArgumentException("Invalid literal.");
            }

            index += literal.length();
            return value;
        }

        private void readDigits() {
            int digitsStart = index;

            while (!isAtEnd() && Character.isDigit(source.charAt(index))) {
                index += 1;
            }

            if (digitsStart == index) {
                throw new IllegalArgumentException("Expected digit.");
            }
        }

        private void expect(char character) {
            skipWhitespace();

            if (isAtEnd() || source.charAt(index) != character) {
                throw new IllegalArgumentException("Expected '" + character + "'.");
            }

            index += 1;
        }

        private boolean peek(char character) {
            skipWhitespace();
            return !isAtEnd() && source.charAt(index) == character;
        }

        private void skipWhitespace() {
            while (!isAtEnd() && Character.isWhitespace(source.charAt(index))) {
                index += 1;
            }
        }

        private boolean isAtEnd() {
            return index >= source.length();
        }
    }
}
