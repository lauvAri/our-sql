package executor.systemCatalog;

import executor.common.Table;
import executor.common.TableSchema;
import executor.storageEngine.StorageEngine;
import executor.common.Record;

public class CatalogManager {
    private final StorageEngine storageEngine;
    private Table catalogTable;

    public CatalogManager(StorageEngine storageEngine) {
        this.storageEngine = storageEngine;
        initializeCatalog();
    }

    /**
     * 初始化系统目录
     */
    private void initializeCatalog() {
//        try {
            // 尝试打开现有目录表
        this.catalogTable = storageEngine.openTable(SystemCatalog.CATALOG_TABLE_NAME);
//        } catch (TableNotFoundException e) {
//            // 不存在则创建
//            storageEngine.createTable(SystemCatalog.CATALOG_TABLE_NAME,
//                    SystemCatalog.CATALOG_SCHEMA);
//            this.catalogTable = storageEngine.openTable(SystemCatalog.CATALOG_TABLE_NAME);
//        }
    }

    /**
     * 注册新表到系统目录
     */
    public void registerTable(String tableName, TableSchema schema) {
        // 构造目录记录
        Record record = new CatalogRecord(
                tableName,
                schema.toJson(),  // 将表结构转为JSON存储
                System.currentTimeMillis()
        ).toRecord();

        // 写入目录表
        catalogTable.insert(record);
    }

    /**
     * 获取表结构
     */
    public TableSchema getTableSchema(String tableName) {
        Record record = catalogTable.getRecord(tableName);
        return TableSchema.fromJson(record.getJsonString("schema_json"));
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        return catalogTable.getRecord(tableName) != null;
    }
}
