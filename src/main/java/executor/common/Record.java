package executor.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    /**
     * 从另一个记录更新字段值（创建新Record实例）
     * @param newRecord 提供新字段值的记录
     * @return 包含合并后字段的新Record
     */
    public Record updateFrom(Record newRecord) {
        Objects.requireNonNull(newRecord, "New record cannot be null");

        // 创建字段的深拷贝（避免修改原始Map）
        Map<String, Object> mergedFields = new HashMap<>(this.fields);

        // 用新值覆盖旧值
        mergedFields.putAll(newRecord.fields());

        return new Record(mergedFields);
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
