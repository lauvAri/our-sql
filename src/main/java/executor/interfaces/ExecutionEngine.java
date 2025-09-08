package executor.interfaces;

/*
执行引擎接口
 */

import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;
import executor.persistence.ExecutionResult;

import executor.common.ExecutionException;

import java.util.List;
import java.util.stream.Collectors;

public class ExecutionEngine {
    private final StorageEngine storage;

    public ExecutionEngine(StorageEngine storage) {
        this.storage = storage;
    }

    public ExecutionResult execute(LogicalPlan plan) {
        try {
            storage.beginTransaction();
            Object result = dispatchExecution(plan);
            storage.commitTransaction();
            return ExecutionResult.success(result);
        } catch (ExecutionException e) {
            storage.rollbackTransaction();
            return ExecutionResult.failure(e.getMessage());
        }
    }

    private Object dispatchExecution(LogicalPlan plan) {
        switch (plan.getOperatorType()) {
            case CREATE_TABLE:
                return executeCreateTable((CreateTablePlan) plan);
            case INSERT:
                return executeInsert((InsertPlan) plan);
            case SELECT:
                return executeSelect((SelectPlan) plan);
            case DELETE:
                return executeDelete((DeletePlan) plan);
            default:
                throw new ExecutionException("Unsupported operator: " + plan.getOperatorType());
        }
    }

    private int executeCreateTable(CreateTablePlan plan) {
        if (storage.tableExists(plan.getTableName())) {
            throw new ExecutionException("Table already exists: " + plan.getTableName());
        }

        List<ColumnDefinition> columns = plan.getColumns().stream()
                .map(col -> new ColumnDefinition(
                        col.getName(),
                        convertType(col.getType()),
                        col.getLength()
                ))
                .collect(Collectors.toList());

        TableSchema schema = new TableSchema(plan.getTableName(), columns);
        storage.createTable(schema);
        return 1; // 返回影响的行数
    }

    private ColumnType convertType(String logicalType) {
        return switch (logicalType.toUpperCase()) {
            case "INT" -> ColumnType.INT;
            case "VARCHAR" -> ColumnType.VARCHAR;
            case "BOOLEAN" -> ColumnType.BOOLEAN;
            case "FLOAT" -> ColumnType.FLOAT;
            default -> throw new ExecutionException("Unsupported type: " + logicalType);
        };
    }
}


