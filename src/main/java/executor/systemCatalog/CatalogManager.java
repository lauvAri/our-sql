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
        try {
            // 尝试打开现有目录表
            this.catalogTable = storageEngine.openTable(SystemCatalog.CATALOG_TABLE_NAME);
            
            // 如果目录表不存在，则创建
            if (this.catalogTable == null) {
                System.out.println("系统目录表不存在，正在创建...");
                storageEngine.createTable(SystemCatalog.CATALOG_SCHEMA);
                this.catalogTable = storageEngine.openTable(SystemCatalog.CATALOG_TABLE_NAME);
                System.out.println("系统目录表创建完成");
            } else {
                System.out.println("找到现有系统目录表");
            }
        } catch (Exception e) {
            System.err.println("初始化系统目录失败: " + e.getMessage());
            e.printStackTrace();
        }
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
        System.out.println("DEBUG: 正在查找表: " + tableName);
        Record record = catalogTable.getRecord(tableName);
        if (record == null) {
            System.out.println("DEBUG: 找不到表记录: " + tableName);
            return null;
        }
        System.out.println("DEBUG: 找到表记录: " + record);
        String schemaJson = record.getJsonString("schema_json");
        System.out.println("DEBUG: 表结构JSON: " + schemaJson);
        try {
            TableSchema result = TableSchema.fromJson(schemaJson);
            System.out.println("DEBUG: JSON解析成功，表结构: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("DEBUG: JSON解析失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        return catalogTable.getRecord(tableName) != null;
    }
}
