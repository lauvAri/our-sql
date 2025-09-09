package common.plan;

import executor.expression.Expression;

public class DeletePlan extends LogicalPlan {
    private final String tableName;
    private final Expression filter; // WHERE 条件

    public DeletePlan(String tableName, Expression filter) {
        this.tableName = tableName;
        this.filter = filter;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.DELETE;
    }

    // Getter 方法
    public String getTableName() { return tableName; }
    public Expression getFilter() { return filter; }
}

