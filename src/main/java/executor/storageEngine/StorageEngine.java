package executor.storageEngine;

/*
存储引擎接口
 */

import executor.common.Table;
import executor.common.TableSchema;

import java.util.List;

/**
 * 存储引擎接口
 */
public interface StorageEngine {
    // 表操作
    Table openTable(String tableName);
    void createTable(TableSchema schema);
    void dropTable(String tableName);
    void createIndex(String tableName, String indexName, List<String> columns, boolean unique);
    void dropIndex(String tableName, String indexName);

    // 事务控制
    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();

    // 实用方法
    boolean tableExists(String tableName);
}
