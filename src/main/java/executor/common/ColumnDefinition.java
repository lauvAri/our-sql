package executor.common;

import executor.interfaces.ColumnType;

public record ColumnDefinition(
        String name,
        ColumnType type,
        int length
) {}
