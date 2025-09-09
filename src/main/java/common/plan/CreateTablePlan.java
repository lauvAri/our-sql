package common.plan;

import common.Column;

import java.util.List;

public class CreateTablePlan extends LogicalPlan {
    private final String tableName;
    private final List<Column> columns;

    public CreateTablePlan(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns); // 防御性拷贝
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.CREATE_TABLE;
    }

    // Getter 方法
    public String getTableName() { return tableName; }
    public List<Column> getColumns() { return columns; }
}



