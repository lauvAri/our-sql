package parser;

import common.plan.*;
import executor.systemCatalog.CatalogManager;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import storage.service.StorageService;
import parser.semantic.*;

/**
 * 简化的Main类，使用SQLCompiler接口
 */
public class Main {
    public static void main(String[] args) {
        try {
            // 创建存储服务和存储引擎
            String dbFileName = "test_database.db";
            String idxFileName = "test_index.idx";
            StorageService storageService = new StorageService(dbFileName, idxFileName);
            StorageEngine storageEngine = new StorageEngineImpl(storageService);
            
            // 创建数据库目录和适配器
            CatalogManager catalogManager = new CatalogManager(storageEngine);
            CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);
            
            // 创建SQL编译器
            SQLCompiler compiler = new SQLCompiler(catalogAdapter);
            
            System.out.println("=== SQL编译器简单演示 ===");
            
            // 第1步：创建表
            String createSQL = "CREATE TABLE student(id INT, name VARCHAR(50), age INT);";
            System.out.println("1. 正在创建表: " + createSQL);
            
            CreateTablePlan createPlan = compiler.compileCreateTable(createSQL);
            System.out.println("   ✅ 编译成功 - 表名: " + createPlan.getTableName() + ", 列数: " + createPlan.getColumns().size());
            
            // 显示详细的列信息
            System.out.println("   列详情:");
            for (int i = 0; i < createPlan.getColumns().size(); i++) {
                common.Column col = createPlan.getColumns().get(i);
                System.out.println("     列" + (i+1) + ": " + col.getName() + " (" + col.getType() + 
                    ", 长度:" + col.getLength() + ", 主键:" + (col.isPrimaryKey() ? "是" : "否") + ")");
            }

            // 第2步：手动注册表到系统目录（为了演示）
            executor.common.TableSchema.Builder builder = new executor.common.TableSchema.Builder().tableName("student");
            builder.addColumn("id", executor.common.ColumnType.INT, 4, true);
            builder.addColumn("name", executor.common.ColumnType.VARCHAR, 50, false);
            builder.addColumn("age", executor.common.ColumnType.INT, 4, false);
            executor.common.TableSchema schema = builder.build();
            catalogAdapter.registerTable("student", schema);
            System.out.println("   ✅ 表已注册到系统目录");

            // 第3步：查询表
            String selectSQL = "SELECT id, name FROM student WHERE age > 18;";
            System.out.println("\n2. 正在查询表: " + selectSQL);

            SelectPlan selectPlan = compiler.compileSelect(selectSQL);
            System.out.println("   ✅ 编译成功 - 查询表: " + selectPlan.getTableName());
            System.out.println("   - 选择列: " + selectPlan.getColumns());
            System.out.println("   - 过滤条件: " + selectPlan.getFilter());

            // 第4步：插入数据
            String insertSQL = "INSERT INTO student (id, name, age) VALUES (1, 'Alice', 20);";
            System.out.println("\n3. 正在插入数据: " + insertSQL);

            InsertPlan insertPlan = compiler.compileInsert(insertSQL);
            System.out.println("   ✅ 编译成功 - 插入表: " + insertPlan.getTableName());
            System.out.println("   - 插入值: " + insertPlan.getValues());

            // 第5步：删除数据
            String deleteSQL = "DELETE FROM student WHERE id = 1;";
            System.out.println("\n4. 正在删除数据: " + deleteSQL);

            DeletePlan deletePlan = compiler.compileDelete(deleteSQL);
            System.out.println("   ✅ 编译成功 - 删除表: " + deletePlan.getTableName());
            System.out.println("   - 删除条件: " + deletePlan.getFilter());

            System.out.println("\n🎉 SQL编译器演示完成！所有操作都成功编译。");
            
        } catch (SQLCompilerException e) {
            System.err.println("❌ SQL编译失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 运行时错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
