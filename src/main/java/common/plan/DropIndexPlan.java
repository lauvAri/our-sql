package common.plan;

public class DropIndexPlan extends LogicalPlan {
    private final String indexName;
    private final String tableName;

    public DropIndexPlan(String indexName, String tableName) {
        this.indexName = indexName;
        this.tableName = tableName;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.DROP_INDEX;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIndexName() {
        return indexName;
    }
}
