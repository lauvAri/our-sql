package executor.common;

import executor.common.ColumnType;

public record ColumnDefinition(
        String name,
        ColumnType type,
        int length
) {
}
