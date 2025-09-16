package executor.executionEngine.engineMethods;

import executor.common.ExecutionException;
import executor.common.Record;
import executor.expression.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * WHERE过滤器
 */
public class EvaluateFilter {
    //WHERE过滤
    public boolean evaluateFilter(Expression filter, Record record) {
        if (filter == null) {
            return false; // 空表达式默认返回 false
        }

        // 递归计算表达式树
        if (filter instanceof BinaryExpression) {
            BinaryExpression expr = (BinaryExpression) filter;
            Object left = evaluateExpression(expr.getLeft(), record);
            Object right = evaluateExpression(expr.getRight(), record);
            if (left == null || right == null) {
                throw new ExecutionException("Cannot compare null values");
            }

            // 统一转为 String 再比较
            String leftStr = left.toString();
            String rightStr = right.toString();

            return switch (expr.getOperator()) {
                case EQ -> Objects.equals(leftStr, rightStr);
                case NEQ -> !Objects.equals(leftStr, rightStr);
                case GT -> compare(leftStr, rightStr) > 0;
                case LT -> compare(leftStr, rightStr) < 0;
                case GTE -> compare(leftStr, rightStr) >= 0;
                case LTE -> compare(leftStr, rightStr) <= 0;
                case AND -> toBoolean(leftStr) && toBoolean(rightStr);
                case OR -> toBoolean(leftStr) || toBoolean(rightStr);
                default -> throw new ExecutionException("Unsupported operator: " + expr.getOperator());
            };
        }
        else if (filter instanceof UnaryExpression) {
            UnaryExpression expr = (UnaryExpression) filter;
            Object value = evaluateExpression(expr.getOperand(), record);

            return switch (expr.getOperator()) {
                case NOT -> !toBoolean(value);
                case IS_NULL -> value == null;
                case IS_NOT_NULL -> value != null;
                default -> throw new ExecutionException("Unsupported unary operator: " + expr.getOperator());
            };
        }
        else if (filter instanceof ColumnReference) {
            // 直接引用列名时，非 null 值视为 true
            Object value = record.getValue(((ColumnReference) filter).getColumnName());
            return value != null;
        }
        else if (filter instanceof Literal) {
            // 常量表达式直接求值
            Object value = ((Literal) filter).getValue();
            return toBoolean(value);
        }
        else {
            throw new ExecutionException("Unsupported expression type: " + filter.getClass().getSimpleName());
        }
    }

    //评估表达式
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
        } else if (expr instanceof ColumnReference) {  // 新增处理 ColumnReference
            return record.getValue(((ColumnReference) expr).getColumnName());
        }else {
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

    // 安全转换为 boolean（支持常见真值逻辑）
    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return true; // 其他非 null 对象视为 true
    }
}
