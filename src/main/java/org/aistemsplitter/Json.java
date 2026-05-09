package org.aistemsplitter;

import java.util.LinkedHashMap;
import java.util.Map;

final class Json {
    private Json() {
    }

    static ObjectBuilder object() {
        return new ObjectBuilder();
    }

    static boolean booleanField(String json, String field) {
        String raw = rawField(json, field);
        return "true".equals(raw);
    }

    static int intField(String json, String field) {
        String raw = rawField(json, field);
        return raw == null || raw.isEmpty() ? 0 : Integer.parseInt(raw);
    }

    static String stringField(String json, String field) {
        String raw = rawField(json, field);
        if (raw == null || "null".equals(raw)) {
            return null;
        }
        if (raw.length() >= 2 && raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"') {
            return unescape(raw.substring(1, raw.length() - 1));
        }
        return raw;
    }

    static String objectField(String json, String field) {
        String value = objectFieldOrNull(json, field);
        return value == null ? "{}" : value;
    }

    static String objectFieldOrNull(String json, String field) {
        String raw = rawField(json, field);
        return raw != null && raw.startsWith("{") ? raw : null;
    }

    static Map<String, String> stringMapField(String json, String field) {
        String object = objectFieldOrNull(json, field);
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (object == null || object.length() <= 2) {
            return map;
        }
        int index = 1;
        while (index < object.length() - 1) {
            index = skipWhitespaceAndComma(object, index);
            if (index >= object.length() - 1 || object.charAt(index) != '"') {
                break;
            }
            int keyEnd = findStringEnd(object, index);
            String key = unescape(object.substring(index + 1, keyEnd));
            index = skipWhitespace(object, keyEnd + 1);
            if (index < object.length() && object.charAt(index) == ':') {
                index++;
            }
            index = skipWhitespace(object, index);
            String value;
            if (index < object.length() && object.charAt(index) == '"') {
                int valueEnd = findStringEnd(object, index);
                value = unescape(object.substring(index + 1, valueEnd));
                index = valueEnd + 1;
            } else {
                int valueEnd = findValueEnd(object, index);
                value = object.substring(index, valueEnd);
                index = valueEnd;
            }
            map.put(key, value);
        }
        return map;
    }

    private static String rawField(String json, String field) {
        String needle = "\"" + field + "\"";
        int fieldIndex = json.indexOf(needle);
        if (fieldIndex < 0) {
            return null;
        }
        int colon = json.indexOf(':', fieldIndex + needle.length());
        if (colon < 0) {
            return null;
        }
        int valueStart = skipWhitespace(json, colon + 1);
        int valueEnd = findValueEnd(json, valueStart);
        return json.substring(valueStart, valueEnd).trim();
    }

    private static int findValueEnd(String json, int start) {
        if (start >= json.length()) {
            return start;
        }
        char first = json.charAt(start);
        if (first == '"') {
            return findStringEnd(json, start) + 1;
        }
        if (first == '{') {
            return findBalancedEnd(json, start, '{', '}') + 1;
        }
        if (first == '[') {
            return findBalancedEnd(json, start, '[', ']') + 1;
        }
        int index = start;
        while (index < json.length() && json.charAt(index) != ',' && json.charAt(index) != '}') {
            index++;
        }
        return index;
    }

    private static int findStringEnd(String json, int start) {
        boolean escaped = false;
        for (int index = start + 1; index < json.length(); index++) {
            char ch = json.charAt(index);
            if (escaped) {
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return index;
            }
        }
        return json.length() - 1;
    }

    private static int findBalancedEnd(String json, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < json.length(); index++) {
            char ch = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }
            if (ch == '"') {
                inString = true;
            } else if (ch == open) {
                depth++;
            } else if (ch == close) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return json.length() - 1;
    }

    private static int skipWhitespaceAndComma(String value, int index) {
        while (index < value.length() && (Character.isWhitespace(value.charAt(index)) || value.charAt(index) == ',')) {
            index++;
        }
        return index;
    }

    private static int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescape(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    static final class ObjectBuilder {
        private final StringBuilder builder = new StringBuilder();
        private boolean first = true;

        ObjectBuilder put(String key, String value) {
            if (value == null) {
                return this;
            }
            appendKey(key);
            builder.append('"').append(escape(value)).append('"');
            return this;
        }

        ObjectBuilder put(String key, long value) {
            appendKey(key);
            builder.append(value);
            return this;
        }

        ObjectBuilder putRaw(String key, String rawJson) {
            appendKey(key);
            builder.append(rawJson);
            return this;
        }

        String toJson() {
            return "{" + builder.toString() + "}";
        }

        private void appendKey(String key) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(key)).append("\":");
        }
    }
}
