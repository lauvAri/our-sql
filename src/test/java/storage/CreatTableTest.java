package storage;

import java.util.List;

import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;
import executor.storageEngine.StorageEngineImpl;
import storage.service.StorageService;

public class CreatTableTest {
    public static void main(String[] args) {
        StorageService storageService = new StorageService("test-create-table.db", "test-create-table.idx");
        StorageEngineImpl engine = new StorageEngineImpl(storageService);
        // 构造表结构
        List<ColumnDefinition> columns = List.of(
            new ColumnDefinition("id", ColumnType.INT, 4),
            new ColumnDefinition("name", ColumnType.VARCHAR, 25)
        );
        TableSchema schema = new TableSchema("user", columns);

        // 调用 createTable
        engine.createTable(schema);

        System.out.println("createTable 测试完成");
    }
}
