package executor.expression;

import executor.common.Record;

/**
 * 表示对记录中某个字段的引用
 */
public final class ColumnReference implements Expression {
    private final String columnName;

    public ColumnReference(String columnName) {
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String toString() {
        return columnName;
    }

    @Override
    public Object evaluate(Record record) {
        return record.getValue(columnName); // 直接返回字段值
    }
}