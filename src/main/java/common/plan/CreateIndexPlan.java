package common.plan;

import java.util.List;

// 创建索引计划
public class CreateIndexPlan extends LogicalPlan {
    private final String indexName;
    private final String tableName;
    private final List<String> columns;
    private final boolean isUnique;

    public CreateIndexPlan(String indexName, String tableName, List<String> columns, boolean isUnique) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columns = columns;
        this.isUnique = isUnique;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.CREATE_INDEX;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean isUnique() {
        return isUnique;
    }
}
