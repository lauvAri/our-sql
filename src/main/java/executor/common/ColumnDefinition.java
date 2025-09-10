package executor.common;

import executor.common.ColumnType;

/**
 * 列定义
 * @param name
 * @param type
 * @param length
 */
public record ColumnDefinition(
        String name,
        ColumnType type,
        int length,
        boolean isPrimaryKey
) {
    public boolean isEmpty() {
        return length == 0;
    }
}
