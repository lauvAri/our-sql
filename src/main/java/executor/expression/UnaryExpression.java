package executor.expression;

import executor.common.Record;

/**
 * 一元表达式
 */
public class UnaryExpression implements Expression {
    public enum Operator {
        NOT("NOT"), IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL");

        private final String symbol;
        Operator(String symbol) { this.symbol = symbol; }
        public String getSymbol() { return symbol; }
    }

    private final Expression operand;
    private final Operator operator;

    public UnaryExpression(Operator operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public Object evaluate(Record record) {
        Object value = operand.evaluate(record);

        switch (operator) {
            case NOT: return !(Boolean)value;
            case IS_NULL: return value == null;
            case IS_NOT_NULL: return value != null;
            default: throw new UnsupportedOperationException("Unknown operator: " + operator);
        }
    }

    public Expression getOperand() {
        return operand;
    }

    public Operator getOperator() {
        return operator;
    }
}
