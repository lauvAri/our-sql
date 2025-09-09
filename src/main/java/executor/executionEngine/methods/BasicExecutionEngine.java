package executor.executionEngine.methods;

import common.plan.CreateTablePlan;
import common.plan.DeletePlan;
import common.plan.InsertPlan;
import common.plan.SelectPlan;
import executor.common.*;
import executor.common.Record;
import executor.storageEngine.StorageEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BasicExecutionEngine {
    //建表
    public static int executeCreateTable(StorageEngine storage,CreateTablePlan plan) {
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

    //类型转换
    private static ColumnType convertType(String logicalType) {
        return switch (logicalType.toUpperCase()) {
            case "INT" -> ColumnType.INT;
            case "VARCHAR" -> ColumnType.VARCHAR;
            case "BOOLEAN" -> ColumnType.BOOLEAN;
            case "FLOAT" -> ColumnType.FLOAT;
            default -> throw new ExecutionException("Unsupported type: " + logicalType);
        };
    }

    //插入
    public static int executeInsert(StorageEngine storage,InsertPlan plan) {
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

    //转换类型
    private static Object convertValue(ColumnType type, Object value) {
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

    //选择
    public static List<Record> executeSelect(StorageEngine storage,SelectPlan plan) {
        Table table = storage.openTable(plan.getTableName());
        List<Record> results = new ArrayList<>();
        EvaluateFilter evaluateFilter = new EvaluateFilter();

        try (CloseableIterator<Record> iterator = table.scan()) {
            while (iterator.hasNext()) {
                Record record = iterator.next();

                // 应用WHERE条件
                if (plan.getFilter() != null &&
                        !evaluateFilter.evaluateFilter(plan.getFilter(), record)) {
                    continue;
                }

                // 应用SELECT投影
                results.add(projectColumns(record, plan.getColumns()));
            }
        }

        return results;
    }

    //投影
    private static Record projectColumns(Record source, List<String> columns) {
        Map<String, Object> projected = new HashMap<>();
        for (String col : columns) {
            projected.put(col, source.fields().get(col));
        }
        return new Record(projected);
    }

    //删除
    public static int executeDelete(StorageEngine storage,DeletePlan plan) {
        Table table = storage.openTable(plan.getTableName());
        AtomicInteger count = new AtomicInteger();
        EvaluateFilter evaluateFilter = new EvaluateFilter();

        // 使用谓词下推优化
        table.delete(record -> {
            if (evaluateFilter.evaluateFilter(plan.getFilter(), record)) {
                count.incrementAndGet();
                return true;
            }
            return false;
        });
        return count.get();
    }
}
