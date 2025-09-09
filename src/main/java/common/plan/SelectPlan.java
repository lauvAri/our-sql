package common.plan;

import java.beans.Expression;
import java.util.List;

public class SelectPlan extends LogicalPlan {
    private final String tableName;
    private final List<String> columns;
    private final Expression filter; // WHERE 条件表达式

    public SelectPlan(String tableName, List<String> columns, Expression filter) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
        this.filter = filter;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.SELECT;
    }

    // Getter 方法
    public String getTableName() { return tableName; }
    public List<String> getColumns() { return columns; }
    public Expression getFilter() { return filter; }
}

