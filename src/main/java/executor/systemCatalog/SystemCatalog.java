package executor.systemCatalog;

import executor.common.ColumnType;
import executor.common.TableSchema;

/**
 * 系统目录
 */
/*
    todo 将系统目录融合进存储引擎 使系统目录通过存储引擎读写
 */
public class SystemCatalog {
    // 系统表名称常量
    public static final String CATALOG_TABLE_NAME = "sys_catalog";

    // 系统表结构定义
    public static final TableSchema CATALOG_SCHEMA = new TableSchema.Builder()
            .addColumn("table_name", ColumnType.VARCHAR, 256,true)  // 主键
            .addColumn("schema_json", ColumnType.VARCHAR, 256)      // 表结构的JSON表示
            .addColumn("created_at", ColumnType.TIMESTAMP, 256)
            .build();
}
