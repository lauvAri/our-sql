package executor.expression;

import executor.common.Record;

public class ConstantExpression implements Expression {
    private final Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    @Override
    public Object evaluate(Record record) {
        return value;
    }

    public Object getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }
}

