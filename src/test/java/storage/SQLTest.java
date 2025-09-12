package storage;

import common.BPTree.BPTree;
import common.plan.CreateTablePlan;
import executor.executionEngine.ExecutionEngine;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import executor.systemCatalog.CatalogManager;
import parser.semantic.CatalogAdapter;
import parser.semantic.SQLCompiler;
import parser.semantic.SQLCompilerException;
import storage.service.StorageService;

public class SQLTest {
    public static void main(String[] args) throws SQLCompilerException {
        // 创建存储服务和存储引擎
        String dbFileName = "test_database.db";
        String idxFileName = "test_index.idx";
        StorageService storageService = new StorageService(dbFileName, idxFileName);
        StorageEngine storageEngine = new StorageEngineImpl(storageService);
        ExecutionEngine executionEngine = new ExecutionEngine(storageEngine);

        // 创建数据库目录和适配器
        CatalogManager catalogManager = new CatalogManager(storageEngine);
        CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);
        // 创建SQL编译器
        SQLCompiler compiler = new SQLCompiler(catalogAdapter);

        String createSQL = "CREATE TABLE student(id INT, name VARCHAR(50), age INT);";
        System.out.println("1. 正在创建表: " + createSQL);

        CreateTablePlan createPlan = compiler.compileCreateTable(createSQL);
        executionEngine.execute(createPlan);
    }
}
