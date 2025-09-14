package executor.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import executor.common.ColumnDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 不可变的表结构
 * @param tableName
 * @param columns
 */
public record TableSchema(
        String tableName,
        List<ColumnDefinition> columns
) implements Serializable {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public List<ColumnDefinition> columns() {
        return columns;
    }

    @Override
    public String tableName() {
        return tableName;
    }

    public ColumnDefinition getColumn(String columnName) {
        for (ColumnDefinition column : columns) {
            if (column.name().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * 序列化为 JSON 字符串
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化 TableSchema 失败", e);
        }
    }

    /**
     * 从 JSON 字符串反序列化
     */
    public static TableSchema fromJson(String json) {
        try {
            return objectMapper.readValue(json, TableSchema.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析 TableSchema JSON 失败", e);
        }
    }

    /**
     * 验证记录是否符合表结构定义
     * @param record 待验证的记录
     * @return 是否验证通过
     */
    public boolean validate(Record record) {
       /*
       todo 增加必填字段检查
       */

//        // 检查必填字段
//        for (String field : requiredFields) {
//            if (!record.containsField(field)) {
//                return false;
//            }
//        }

        // 检查字段类型
        for (ColumnDefinition column : columns) {
            String field = column.name();
            ColumnType expectedType = column.type();
            Object value = record.getValue(field);

            if (value != null && !expectedType.getJavaType().isInstance(value)) {
                return false;
            }
        }

        return true;
    }

    // 新增 Builder 类
    public static class Builder {
        private String tableName;
        private final List<ColumnDefinition> columns = new ArrayList<>();
        private List<String> primaryKeys = new ArrayList<>();

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder addColumn(String name, ColumnType type,int len, boolean isPrimaryKey) {
            columns.add(new ColumnDefinition(name, type, len, isPrimaryKey));
            return this;
        }

        public Builder addColumn(String name, ColumnType type,int len) {
            return addColumn(name, type, len, false);  // 默认非主键
        }

        public TableSchema build() {
            return new TableSchema(tableName, List.copyOf(columns));  // 确保不可变
        }

        public Builder columns(List<ColumnDefinition> columns) {
            this.columns.addAll(columns);
            return this;
        }

        public Builder primaryKeys(List<String> primaryKeys) {
            this.primaryKeys = primaryKeys;
            return this;
        }
    }
}