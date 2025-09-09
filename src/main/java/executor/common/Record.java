package executor.common;

import java.util.Map;

public record Record(Map<String, Object> fields) {
    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public Object getValue(String fieldName) {
        return fields.get(fieldName);
    }
}
