package executor.storageEngine;

import executor.common.Table;
import executor.common.TableSchema;

import java.util.List;

/**
 * 存储引擎
 */
public class StorageEngineImpl implements StorageEngine {
    @Override
    public Table openTable(String tableName) {
        return null;
    }

    @Override
    public void createTable(TableSchema schema) {

    }

    @Override
    public void dropTable(String tableName) {

    }

    @Override
    public void createIndex(String tableName, String indexName, List<String> columns, boolean unique) {

    }

    @Override
    public void dropIndex(String tableName, String indexName) {

    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public boolean tableExists(String tableName) {
        return false;
    }
}
