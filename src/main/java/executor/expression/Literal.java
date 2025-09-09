package executor.expression;

import executor.common.Record;

/**
 * 表示一个常量值（如数字、字符串、布尔值等）
 */
public final class Literal implements Expression {
    private final Object value;

    public Literal(Object value) {
        this.value = value; // 允许 null（表示 NULL 常量）
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value == null ? "NULL" : value.toString();
    }

    @Override
    public Object evaluate(Record record) {
        // 实现逻辑：直接返回字面量的值，忽略 Record
        return value;
    }
}

