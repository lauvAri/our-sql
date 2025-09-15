package storage;

import com.sun.jdi.request.ExceptionRequest;
import common.plan.CreateTablePlan;
import common.plan.DeletePlan;
import common.plan.InsertPlan;
import common.plan.SelectPlan;
import executor.common.Table;
import executor.common.TableSchema;
import executor.executionEngine.ExecutionEngine;
import executor.executionEngine.ExecutionResult;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import executor.systemCatalog.CatalogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.semantic.CatalogAdapter;
import parser.semantic.SQLCompiler;
import parser.semantic.SQLCompilerException;
import storage.service.MyStorageService;
import storage.service.StorageService;
import store.StoreManager;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class SQLTest {
    private static final Logger logger = LoggerFactory.getLogger(SQLTest.class);
    public static void main(String[] args) throws SQLCompilerException, IOException {
        // 创建存储服务和存储引擎
//        String dbFileName = "test_database.db";
//        String idxFileName = "test_index.idx";
//        StorageService storageService = new StorageService(dbFileName, idxFileName);

//        StorageEngine storageEngine = new PageBasedStorageEngine("student.db");
//        StorageEngine storageEngine = new StorageEngineImpl(storageService);

//        StorageEngine storageEngine = new SimpleStorageEngine();

//        MyStorageService myStorageService = new MyStorageService();
//        StorageEngine storageEngine = new StorageEngineImpl(myStorageService);
        ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, TableSchema> schemas = new ConcurrentHashMap<>();

        StoreManager storeManager = new StoreManager(tables, schemas);
        StorageEngine storageEngine = new StorageEngineImpl(storeManager);

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

//        // 第2步：手动注册表到系统目录（为了演示）
//        executor.common.TableSchema.Builder builder = new executor.common.TableSchema.Builder().tableName("student");
//        builder.addColumn("id", executor.common.ColumnType.INT, 4, true);
//        builder.addColumn("name", executor.common.ColumnType.VARCHAR, 50, false);
//        builder.addColumn("age", executor.common.ColumnType.INT, 4, false);
//        executor.common.TableSchema schema = builder.build();
//        catalogAdapter.registerTable("student", schema);
//        logger.info(catalogAdapter.tableExists("student")? "register":"register failed");
//        System.out.println("   ✅ 表已注册到系统目录");
//
//        logger.info(catalogAdapter.tableExists("student")? "register":"register failed");



        // 第4步：插入数据
        String insertSQL = "INSERT INTO student (id, name, age) VALUES (1, 'Alice', 20);";
        System.out.println("\n3. 正在插入数据: " + insertSQL);

        InsertPlan insertPlan = compiler.compileInsert(insertSQL);
        executionEngine.execute(insertPlan);

        insertSQL = "INSERT INTO student (id, name, age) VALUES (2, 'Alice', 20);";
        System.out.println("\n3. 正在插入数据: " + insertSQL);

        insertPlan = compiler.compileInsert(insertSQL);
        executionEngine.execute(insertPlan);

        insertSQL = "INSERT INTO student (id, name, age) VALUES (3, 'Alice', 20);";
        System.out.println("\n3. 正在插入数据: " + insertSQL);

        insertPlan = compiler.compileInsert(insertSQL);
        executionEngine.execute(insertPlan);

        String deleteSQL = "DELETE FROM student WHERE id = 1;";
        System.out.println("\n4. 正在删除数据: " + deleteSQL);

        DeletePlan deletePlan = compiler.compileDelete(deleteSQL);
        System.out.println("   ✅ 编译成功 - 删除表: " + deletePlan.getTableName());
        System.out.println("   - 删除条件: " + deletePlan.getFilter());
        executionEngine.execute(deletePlan);

        // 第3步：查询表
        String selectSQL = "SELECT id, name FROM student WHERE age > 18;";
        System.out.println("\n2. 正在查询表: " + selectSQL);

        SelectPlan selectPlan = compiler.compileSelect(selectSQL);
        System.out.println("   ✅ 编译成功 - 查询表: " + selectPlan.getTableName());
        System.out.println("   - 选择列: " + selectPlan.getColumns());
        System.out.println("   - 过滤条件: " + selectPlan.getFilter());
        ExecutionResult result = executionEngine.execute(selectPlan);
        logger.info("data: {}", result.getData()) ;
        storeManager.close();
    }
}
