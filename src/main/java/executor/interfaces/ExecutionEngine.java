package executor.interfaces;

/*
执行引擎接口
 */

import common.plan.*;
import executor.common.*;
import executor.expression.*;
import executor.persistence.ExecutionResult;
import executor.common.Record;

import java.util.*;
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

    private Object evaluateExpression(Expression expr, Record record) {
        if (expr == null) {
            throw new ExecutionException("Expression cannot be null");
        }

        // 递归评估所有表达式类型
        if (expr instanceof FieldExpression) {
            return evaluateFieldExpression((FieldExpression) expr, record);
        } else if (expr instanceof ConstantExpression) {
            return evaluateConstantExpression((ConstantExpression) expr);
        } else if (expr instanceof BinaryExpression) {
            return evaluateBinaryExpression((BinaryExpression) expr, record);
        } else if (expr instanceof UnaryExpression) {
            return evaluateUnaryExpression((UnaryExpression) expr, record);
        } else if (expr instanceof FunctionExpression) {
            return evaluateFunctionExpression((FunctionExpression) expr, record);
        } else {
            throw new ExecutionException("Unsupported expression type: " + expr.getClass());
        }
    }

    // 字段表达式评估
    private Object evaluateFieldExpression(FieldExpression expr, Record record) {
        Object value = record.getField(expr.getFieldName());
        if (value == null && !record.hasField(expr.getFieldName())) {
            throw new ExecutionException("Field not found: " + expr.getFieldName());
        }
        return value;
    }

    // 常量表达式评估
    private Object evaluateConstantExpression(ConstantExpression expr) {
        return expr.getValue();
    }

    // 二元表达式评估
    private Object evaluateBinaryExpression(BinaryExpression expr, Record record) {
        Object left = evaluateExpression(expr.getLeft(), record);
        Object right = evaluateExpression(expr.getRight(), record);

        switch (expr.getOperator()) {
            case EQ: return Objects.equals(left, right);
            case NEQ: return !Objects.equals(left, right);
            case GT: return compare(left, right) > 0;
            case LT: return compare(left, right) < 0;
            case GTE: return compare(left, right) >= 0;
            case LTE: return compare(left, right) <= 0;
            case AND: return (Boolean)left && (Boolean)right;
            case OR: return (Boolean)left || (Boolean)right;
            default: throw new ExecutionException("Unsupported operator: " + expr.getOperator());
        }
    }

    // 一元表达式评估
    private Object evaluateUnaryExpression(UnaryExpression expr, Record record) {
        Object value = evaluateExpression(expr.getOperand(), record);

        switch (expr.getOperator()) {
            case NOT: return !(Boolean)value;
            case IS_NULL: return value == null;
            case IS_NOT_NULL: return value != null;
            default: throw new ExecutionException("Unsupported operator: " + expr.getOperator());
        }
    }

    // 函数表达式评估
    private Object evaluateFunctionExpression(FunctionExpression expr, Record record) {
        List<Object> args = expr.getArguments().stream()
                .map(arg -> evaluateExpression(arg, record))
                .collect(Collectors.toList());

        switch (expr.getFunctionName().toUpperCase()) {
            case "UPPER": return args.get(0).toString().toUpperCase();
            case "LOWER": return args.get(0).toString().toLowerCase();
            case "LENGTH": return args.get(0).toString().length();
            case "SUBSTR":
                String str = args.get(0).toString();
                int start = (Integer)args.get(1);
                int length = (Integer)args.get(2);
                return str.substring(start, Math.min(start + length, str.length()));
            default:
                throw new ExecutionException("Unsupported function: " + expr.getFunctionName());
        }
    }

    // 比较辅助方法
    private int compare(Object left, Object right) {
        if (left == null || right == null) {
            throw new ExecutionException("Cannot compare null values");
        }
        if (!(left instanceof Comparable) || !(right instanceof Comparable)) {
            throw new ExecutionException("Values are not comparable: "
                    + left.getClass() + " and " + right.getClass());
        }
        return ((Comparable)left).compareTo(right);
    }

}