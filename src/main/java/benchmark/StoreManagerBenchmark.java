package benchmark;

import common.plan.CreateTablePlan;
import common.plan.InsertPlan;
import executor.common.Table;
import executor.common.TableSchema;
import executor.executionEngine.ExecutionEngine;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import executor.systemCatalog.CatalogManager;
import org.openjdk.jmh.annotations.*;
import parser.semantic.CatalogAdapter;
import parser.semantic.SQLCompiler;
import parser.semantic.SQLCompilerException;
import store.StoreManager;

import java.util.concurrent.ConcurrentHashMap;

@State(Scope.Benchmark)
public class StoreManagerBenchmark {
    private StoreManager storeManager;

    @Setup
    public void setup() throws SQLCompilerException {
        // 初始化测试数据

        ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, TableSchema> schemas = new ConcurrentHashMap<>();

        this.storeManager = new StoreManager(tables, schemas);
        StorageEngine storageEngine = new StorageEngineImpl(storeManager);

        ExecutionEngine executionEngine = new ExecutionEngine(storageEngine);

        // 创建数据库目录和适配器
        CatalogManager catalogManager = new CatalogManager(storageEngine);
        CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);
        // 创建SQL编译器
        SQLCompiler compiler = new SQLCompiler(catalogAdapter);


        for (int i = 0; i < 1000; i++) {
            // 创建表
            String createSQL = "CREATE TABLE student_" + i + " (id INT, name VARCHAR(50), age INT);";
            System.out.println("1. 正在创建表: " + createSQL);

            CreateTablePlan createPlan = compiler.compileCreateTable(createSQL);
            executionEngine.execute(createPlan);
        }
    }

    @Benchmark
    public void testClosePerformance() {
        storeManager.close();
    }

    @Benchmark
    public void testLoadAllTables() {
        storeManager.loadAllTables();
    }


}

