package executor.interfaces;

/*
执行引擎接口
 */

import executor.common.*;
import executor.persistence.ExecutionResult;

import java.lang.Record;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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

    private int executeInsert(InsertPlan plan) {
        Table table = storage.openTable(plan.getTableName());
        int count = 0;

        for (List<Object> values : plan.getValues()) {
            Map<String, Object> fieldMap = new HashMap<>();
            TableSchema schema = table.getSchema();

            for (int i = 0; i < schema.columns().size(); i++) {
                ColumnDefinition col = schema.columns().get(i);
                Object value = convertValue(col.type(), values.get(i));
                fieldMap.put(col.name(), value);
            }

            table.insert(new Record(fieldMap));
            count++;
        }

        return count;
    }

    private Object convertValue(ColumnType type, Object value) {
        try {
            return switch (type) {
                case INT -> Integer.parseInt(value.toString());
                case FLOAT -> Double.parseDouble(value.toString());
                case BOOLEAN -> Boolean.parseBoolean(value.toString());
                case VARCHAR -> value.toString();
            };
        } catch (Exception e) {
            throw new ExecutionException("Type conversion failed: " + e.getMessage());
        }
    }

    private List<Record> executeSelect(SelectPlan plan) {
        Table table = storage.openTable(plan.getTableName());
        List<Record> results = new ArrayList<>();

        try (Iterator<Record> iterator = table.scan()) {
            while (iterator.hasNext()) {
                Record record = iterator.next();

                // 应用WHERE条件
                if (plan.getFilter() != null &&
                        !evaluateFilter(plan.getFilter(), record)) {
                    continue;
                }

                // 应用SELECT投影
                results.add(projectColumns(record, plan.getColumns()));
            }
        }

        return results;
    }

    private boolean evaluateFilter(Expression filter, Record record) {
        // 递归计算表达式树
        if (filter instanceof BinaryExpression) {
            BinaryExpression expr = (BinaryExpression) filter;
            Object left = evaluateExpression(expr.getLeft(), record);
            Object right = evaluateExpression(expr.getRight(), record);

            return switch (expr.getOperator()) {
                case "=" -> Objects.equals(left, right);
                case ">" -> ((Comparable) left).compareTo(right) > 0;
                case "<" -> ... // 其他操作符
                default -> throw new ExecutionException("Unsupported operator: " + expr.getOperator());
            };
        }
        // 处理其他表达式类型...
    }

    private Record projectColumns(Record source, List<String> columns) {
        Map<String, Object> projected = new HashMap<>();
        for (String col : columns) {
            projected.put(col, source.fields().get(col));
        }
        return new Record(projected);
    }

    private int executeDelete(DeletePlan plan) {
        Table table = storage.openTable(plan.getTableName());
        AtomicInteger count = new AtomicInteger();

        // 使用谓词下推优化
        table.delete(record -> {
            if (evaluateFilter(plan.getFilter(), record)) {
                count.incrementAndGet();
                return true;
            }
            return false;
        });

        return count.get();
    }
}