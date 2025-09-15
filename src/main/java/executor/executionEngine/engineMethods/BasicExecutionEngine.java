package executor.executionEngine.engineMethods;

import common.plan.CreateTablePlan;
import common.plan.DeletePlan;
import common.plan.InsertPlan;
import common.plan.SelectPlan;
import executor.common.*;
import executor.common.Record;
import executor.common.orderby.OrderByClause;
import executor.executionEngine.func.LimitExecutor;
import executor.executionEngine.func.OrderByExecutor;
import executor.expression.*;
import executor.storageEngine.StorageEngine;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 执行引擎基本操作（增删查改）
 */
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
                        col.getLength(),
                        col.isPrimaryKey()
                ))
                .collect(Collectors.toList());

//        validatePrimaryKey(columns);

        TableSchema schema = new TableSchema.Builder()
                .tableName(plan.getTableName())
                .columns(columns)
                .primaryKeys(getPrimaryKeys(columns))  // 设置主键列名集合
                .build();

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

    private static void validatePrimaryKey(List<ColumnDefinition> columns) {
        long pkCount = columns.stream().filter(ColumnDefinition::isPrimaryKey).count();

        if (pkCount == 0) {
            throw new ExecutionException("Table must have at least one primary key");
        }

        // 可选：检查主键类型是否合法（如BLOB类型不能作为主键）
//        columns.stream()
//                .filter(ColumnDefinition::isPrimaryKey)
//                .forEach(col -> {
//                    if (col.type() == DataType.BLOB) {
//                        throw new ExecutionException(
//                                "BLOB type cannot be primary key: " + col.getName());
//                    }
//                });
    }

    /**
     * 提取主键列名集合
     */
    private static List<String> getPrimaryKeys(List<ColumnDefinition> columns) {
        return columns.stream()
                .filter(ColumnDefinition::isPrimaryKey)
                .map(ColumnDefinition::name)
                .collect(Collectors.toList());
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
        storage.saveTable(plan.getTableName(), table);
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
                case TIMESTAMP -> Timestamp.valueOf(value.toString());
            };
        } catch (Exception e) {
            throw new ExecutionException("Type conversion failed: " + e.getMessage());
        }
    }

    //选择
    public static List<Record> executeSelect(StorageEngine storage, SelectPlan plan) {
        Table table = storage.openTable(plan.getTableName());
        List<Record> results = new ArrayList<>();
        EvaluateFilter evaluateFilter = new EvaluateFilter();

        // 1. 检查是否有可用的索引
        Index usableIndex = findUsableIndex(table, plan.getFilter());

        if (usableIndex != null) {
            // 2. 使用索引扫描代替全表扫描
            try (CloseableIterator<Record> iterator = usableIndex.search(extractIndexKey(plan.getFilter()))) {
                while (iterator.hasNext()) {
                    Record record = iterator.next();

                    // 3. 应用可能的剩余过滤条件（如果索引不完全匹配查询条件）
                    if (plan.getFilter() != null &&
                            !evaluateFilter.evaluateFilter(plan.getFilter(), record)) {
                        continue;
                    }

                    results.add(projectColumns(record, plan.getColumns()));
                }
            }
        } else {
            // 4. 没有可用索引，回退到全表扫描
            try (CloseableIterator<Record> iterator = table.scan()) {
                while (iterator.hasNext()) {
                    Record record = iterator.next();

                    if (plan.getFilter() != null &&
                            !evaluateFilter.evaluateFilter(plan.getFilter(), record)) {
                        continue;
                    }

                    results.add(projectColumns(record, plan.getColumns()));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        System.out.println(results);

        //limit函数
        if(plan.getLimit() >= 0){
            LimitExecutor limitExecutor = new LimitExecutor();
            results = limitExecutor.limit(results, plan.getLimit());
        }

        //OrderBy函数
        if(plan.getOrderBy() != null){
            OrderByExecutor orderByExecutor = new OrderByExecutor();
            results = orderByExecutor.sort(results, plan.getOrderBy());
        }

        System.out.println(results.toString());

        return results;
    }

    //查找可用索引
    private static Index findUsableIndex(Table table, Expression filter) {
        if (filter == null) {
            return null; // 没有过滤条件，不需要索引
        }

        // 获取表中所有索引
        List<Index> indexes = table.getIndexes();

        // 找出最适合的索引
        for (Index index : indexes) {
            if (isIndexApplicable(index, filter)) {
                return index;
            }
        }

        return null;
    }

    //检查索引是否可用
    private static boolean isIndexApplicable(Index index, Expression filter) {
        // 简单实现：检查过滤条件是否直接使用了索引列
        // 实际实现中可以更复杂，考虑多列索引、部分匹配等情况

        Set<String> indexColumns = new HashSet<>(index.getColumns());
        Set<String> filterColumns = extractColumnsFromFilter(filter);

        return !Collections.disjoint(indexColumns, filterColumns);
    }

    //提取索引条件
    private static Object extractIndexKey(Expression filter) {
        // 简单实现：提取等值比较的值
        // 实际实现中需要考虑更复杂的条件解析

        if (filter instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) filter;
            if (binary.getOperator() == BinaryExpression.Operator.EQ &&
                    binary.getRight() instanceof Literal) {
                return ((Literal) binary.getRight()).getValue();
            }
        }

        return null;
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
        storage.saveTable(plan.getTableName(), table);
        return count.get();
    }

    /*
    todo 有更好的实现方式 适用于更复杂的表达式
     */
    //从 SQL 过滤条件表达式中提取所有涉及的列名
    private static Set<String> extractColumnsFromFilter(Expression filter) {
        Set<String> columns = new HashSet<>();
        if (filter instanceof ColumnReference) {
            columns.add(((ColumnReference) filter).getColumnName());
        } else if (filter instanceof BinaryExpression) {
            BinaryExpression binary = (BinaryExpression) filter;
            columns.addAll(extractColumnsFromFilter(binary.getLeft()));
            columns.addAll(extractColumnsFromFilter(binary.getRight()));
        } else if (filter instanceof UnaryExpression) {
            UnaryExpression unary = (UnaryExpression) filter;
            columns.addAll(extractColumnsFromFilter(unary.getOperand()));
        }
        // 添加其他表达式类型的处理...
        return columns;
    }


}
