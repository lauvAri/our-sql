package storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.Table;
import executor.common.TableSchema;
import executor.storageEngine.StorageEngineImpl;
import storage.service.StorageService;

import static org.junit.jupiter.api.Assertions.*;

class StorageEngineTest {
    private static final String DB_FILE = "test_storage_engine.db";
    private static final String IDX_FILE = "test_storage_engine.idx";
    private StorageService storageService;
    private StorageEngineImpl storageEngine;

    @BeforeEach
    void setUp() {
        // 删除之前的测试文件
        new File(DB_FILE).delete();
        new File(IDX_FILE).delete();
        
        // 初始化存储服务和存储引擎
        storageService = new StorageService(DB_FILE, IDX_FILE);
        storageEngine = new StorageEngineImpl(storageService);
    }

    @AfterEach
    void tearDown() throws IOException {
        storageService.flushAllPages();
        // 删除测试文件
        new File(DB_FILE).delete();
        new File(IDX_FILE).delete();
    }

    @Test
    void testCreateTableAndOpenTable() {
        // 构造表结构
        List<ColumnDefinition> columns = List.of(
            new ColumnDefinition("id", ColumnType.INT, 4, true),
            new ColumnDefinition("name", ColumnType.VARCHAR, 255, false),
            new ColumnDefinition("age", ColumnType.INT, 4, false)
        );
        TableSchema schema = new TableSchema("user", columns);

        // 测试创建表
        storageEngine.createTable(schema);

        // 验证表是否存在
        assertTrue(storageEngine.tableExists("user"));

        // 测试打开表
        Table table = storageEngine.openTable("user");
        assertNotNull(table);
        assertEquals(schema.tableName(), table.getSchema().tableName());
        assertEquals(schema.columns().size(), table.getSchema().columns().size());
        
        // 验证列信息
        for (int i = 0; i < schema.columns().size(); i++) {
            ColumnDefinition expectedColumn = schema.columns().get(i);
            ColumnDefinition actualColumn = table.getSchema().columns().get(i);
            assertEquals(expectedColumn.name(), actualColumn.name());
            assertEquals(expectedColumn.type(), actualColumn.type());
            assertEquals(expectedColumn.length(), actualColumn.length());
            assertEquals(expectedColumn.isPrimaryKey(), actualColumn.isPrimaryKey());
        }
    }

    @Test
    void testOpenNonExistentTable() {
        // 尝试打开不存在的表
        Table table = storageEngine.openTable("non_existent_table");
        assertNull(table);
    }

    @Test
    void testTableExists() {
        // 检查不存在的表
        assertFalse(storageEngine.tableExists("non_existent_table"));

        // 创建表
        List<ColumnDefinition> columns = List.of(
            new ColumnDefinition("id", ColumnType.INT, 4, true)
        );
        TableSchema schema = new TableSchema("test_table", columns);
        storageEngine.createTable(schema);

        // 检查存在的表
        assertTrue(storageEngine.tableExists("test_table"));
    }

    @Test
    void testDropTable() {
        // 创建表
        List<ColumnDefinition> columns = List.of(
            new ColumnDefinition("id", ColumnType.INT, 4, true)
        );
        TableSchema schema = new TableSchema("drop_test_table", columns);
        storageEngine.createTable(schema);

        // 验证表存在
        assertTrue(storageEngine.tableExists("drop_test_table"));

        // 删除表
        storageEngine.dropTable("drop_test_table");

        // 验证表已删除
        assertFalse(storageEngine.tableExists("drop_test_table"));
        assertNull(storageEngine.openTable("drop_test_table"));
    }
}
