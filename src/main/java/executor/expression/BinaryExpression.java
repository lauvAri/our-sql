package executor.expression;

import executor.common.Record;

import java.util.Objects;

/**
 * 二元表达式
 */
public class BinaryExpression implements Expression {
    public enum Operator {
        EQ("="), NEQ("<>"), GT(">"), LT("<"),
        GTE(">="), LTE("<="), AND("AND"), OR("OR");

        private final String symbol;
        Operator(String symbol) { this.symbol = symbol; }
        public String getSymbol() { return symbol; }
    }

    private final Expression left;
    private final Operator operator;
    private final Expression right;

    public BinaryExpression(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() { return left; }
    public Operator getOperator() { return operator; }
    public Expression getRight() { return right; }

    @Override
    public Object evaluate(Record record) {
        Object leftValue = left.evaluate(record);
        Object rightValue = right.evaluate(record);

        switch (operator) {
            case EQ: return Objects.equals(leftValue, rightValue);
            case NEQ: return !Objects.equals(leftValue, rightValue);
            case GT: return compare(leftValue, rightValue) > 0;
            case LT: return compare(leftValue, rightValue) < 0;
            case GTE: return compare(leftValue, rightValue) >= 0;
            case LTE: return compare(leftValue, rightValue) <= 0;
            case AND: return (Boolean)leftValue && (Boolean)rightValue;
            case OR: return (Boolean)leftValue || (Boolean)rightValue;
            default: throw new UnsupportedOperationException("Unknown operator: " + operator);
        }
    }

    private int compare(Object left, Object right) {
        if (left instanceof Comparable && right instanceof Comparable) {
            return ((Comparable)left).compareTo(right);
        }
        throw new ClassCastException("Cannot compare " + left.getClass() + " with " + right.getClass());
    }
    
    @Override
    public String toString() {
        return "(" + left + " " + operator.getSymbol() + " " + right + ")";
    }
}

