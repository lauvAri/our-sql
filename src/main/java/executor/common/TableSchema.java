package executor.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.

import executor.common.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 不可变的表结构
 * @param tableName
 * @param columns
 */
public record TableSchema(
        String tableName,
        List<ColumnDefinition> columns
) {
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

    // 新增 Builder 类
    public static class Builder {
        private String tableName;
        private final List<ColumnDefinition> columns = new ArrayList<>();

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

        public static TableSchema fromJson(String json) {
            try {
                return objectMapper.readValue(json, TableSchema.class);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("无效的 JSON 格式", e);
            }
        }
    }
}