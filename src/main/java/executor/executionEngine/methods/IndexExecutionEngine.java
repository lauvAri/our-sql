package executor.executionEngine.methods;

import common.plan.CreateIndexPlan;
import common.plan.DropIndexPlan;
import executor.common.ExecutionException;
import executor.common.Table;
import executor.common.TableSchema;
import executor.storageEngine.StorageEngine;

public class IndexExecutionEngine {
    public static int executeCreateIndex(StorageEngine storage, CreateIndexPlan plan) {
        Table table = storage.openTable(plan.getTableName());

        // 验证索引列是否存在
        TableSchema schema = table.getSchema();
        for (String column : plan.getColumns()) {
            if (schema.getColumn(column)==null) {
                throw new ExecutionException("Column not found: " + column);
            }
        }

        // 创建索引
        storage.createIndex(plan.getIndexName(), plan.getTableName(),
                plan.getColumns(), plan.isUnique());
        return 1; // 返回影响行数
    }

    // 删除索引执行方法
    public static int executeDropIndex(StorageEngine storage,DropIndexPlan plan) {
        storage.dropIndex(plan.getIndexName(), plan.getTableName());
        return 1; // 返回影响行数
    }
}
