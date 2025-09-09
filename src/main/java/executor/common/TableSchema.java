package executor.common;

import executor.common.ColumnDefinition;

import java.util.List;

public record TableSchema(
        String tableName,
        List<ColumnDefinition> columns
) {
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
}