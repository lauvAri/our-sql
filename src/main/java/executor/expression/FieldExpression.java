package executor.expression;

import executor.common.Record;

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
