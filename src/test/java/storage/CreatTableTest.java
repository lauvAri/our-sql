package storage;

import java.util.List;

import common.BPTree.BPTree;
import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;
import executor.storageEngine.StorageEngineImpl;
import storage.service.MyStorageService;
import storage.service.StorageService;

public class CreatTableTest {
    public static void main(String[] args) {
        MyStorageService myStorageService = new MyStorageService();
        executor.common.TableSchema.Builder builder = new executor.common.TableSchema.Builder().tableName("student");
        builder.addColumn("id", executor.common.ColumnType.INT, 4, true);
        builder.addColumn("name", executor.common.ColumnType.VARCHAR, 50, false);
        builder.addColumn("age", executor.common.ColumnType.INT, 4, false);
        executor.common.TableSchema schema = builder.build();
        myStorageService.createTable(schema);
    }
}
