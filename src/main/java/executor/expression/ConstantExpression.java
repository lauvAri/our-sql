package executor.expression;

import executor.common.Record;

/**
 * 常量表达式
 */
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
}

