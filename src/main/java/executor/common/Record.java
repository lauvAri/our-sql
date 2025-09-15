package executor.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonIgnore
    public String getPrimaryKey() {
        /*
        todo 目前主键是写死的 看一看能不能变成活的
        */
        Object id = fields.get("id");  // 假设主键字段名为"id"
        if (id == null) {
            throw new IllegalStateException("记录缺少主键字段(id)");
        }
        return id.toString();  // 确保返回String类型
    }
}
