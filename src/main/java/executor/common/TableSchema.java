package executor.common;

import executor.common.ColumnDefinition;

import java.util.List;

public record TableSchema(
        String tableName,
        List<ColumnDefinition> columns
) {}