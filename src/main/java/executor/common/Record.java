package executor.common;

import java.util.Map;

/**
 * 记录
 * @param fields
 */
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

    public String getJsonString(String columnName) {
        Object value = fields.get(columnName);
        if (value == null) {
            return null; // 或 throw new IllegalArgumentException("字段不存在: " + columnName);
        }

        if (!(value instanceof String)) {
            throw new ClassCastException(
                    String.format("字段 %s 类型不匹配 (应为 String, 实际为 %s)",
                            columnName, value.getClass().getSimpleName())
            );
        }
        return (String) value;
    }
}
