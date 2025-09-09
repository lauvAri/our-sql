package common.plan;

import java.util.List;

public class InsertPlan extends LogicalPlan {
    private final String tableName;
    private final List<List<Object>> values;

    public InsertPlan(String tableName, List<List<Object>> values) {
        this.tableName = tableName;
        this.values = List.copyOf(values);
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.INSERT;
    }

    // Getter 方法
    public String getTableName() { return tableName; }
    public List<List<Object>> getValues() { return values; }
}

