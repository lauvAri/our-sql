package executor.expression;

import executor.common.Record;

/**
 * 在记录（Record）中动态提取指定字段（fieldName）的值
 */
public class FieldExpression implements Expression {
    private final String fieldName;

    public FieldExpression(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Object evaluate(Record record) {
        return record.getField(fieldName);
    }

    public String getFieldName() {
        return fieldName;
    }
}
