package executor.interfaces;

/*
存储引擎接口
 */

import executor.common.Table;
import executor.common.TableSchema;

public interface StorageEngine {
    // 表操作
    Table openTable(String tableName);
    void createTable(TableSchema schema);
    void dropTable(String tableName);

    // 事务控制
    void beginTransaction();
    void commitTransaction();
    void rollbackTransaction();

    // 实用方法
    boolean tableExists(String tableName);
}
