package parser.semantic;

import common.plan.*;
import executor.systemCatalog.CatalogManager;
import executor.storageEngine.StorageEngineImpl;

/**
 * SQL编译器接口演示和测试类
 * 展示如何使用SQLCompiler接口
 */
public class SQLCompilerInterfaceDemo {
    
    public static void main(String[] args) {
        try {
            // 初始化存储服务和存储引擎（参考Main.java中的初始化方式）
            String dbFileName = "test_database.db";
            String idxFileName = "test_index.idx";
            storage.service.StorageService storageService = new storage.service.StorageService(dbFileName, idxFileName);
            executor.storageEngine.StorageEngine storageEngine = new StorageEngineImpl(storageService);
            
            // 创建数据库目录和适配器
            CatalogManager catalogManager = new CatalogManager(storageEngine);
            CatalogAdapter catalog = new CatalogAdapter(catalogManager);
            
            // 初始化SQL编译器
            SQLCompiler compiler = new SQLCompiler(catalog);
            
            System.out.println("=== SQL编译器接口演示 ===");
            System.out.println("✅ 初始化完成\n");
            
            // 先创建测试表
            System.out.println("=== 步骤1: 创建测试表 ===");
            demonstrateCreateTable(compiler);
            
            // 实际执行CREATE TABLE以注册表到目录中
            executeCreateTable(compiler, "CREATE TABLE users (id INT, name VARCHAR, age INT);");
            
            System.out.println("=== 步骤2: 测试查询和修改操作 ===");
            // 演示SELECT编译
            demonstrateSelect(compiler);
            
            // 演示INSERT编译  
            demonstrateInsert(compiler);
            
            // 演示DELETE编译
            demonstrateDelete(compiler);
            
            // 演示错误处理
            demonstrateErrorHandling(compiler);
            
            // 演示验证功能
            demonstrateValidation(compiler);
            
        } catch (Exception e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateCreateTable(SQLCompiler compiler) {
        System.out.println("=== CREATE TABLE 编译演示 ===");
        try {
            String sql = "CREATE TABLE test_demo (id INT, name VARCHAR, age INT);";
            System.out.println("SQL: " + sql);
            
            CreateTablePlan plan = compiler.compileCreateTable(sql);
            System.out.println("✅ 编译成功!");
            System.out.println("计划类型: " + plan.getClass().getSimpleName());
            System.out.println("表名: " + plan.getTableName());
            System.out.println("列数: " + plan.getColumns().size());
            
            // 显示详细的列信息
            System.out.println("列详情:");
            for (int i = 0; i < plan.getColumns().size(); i++) {
                common.Column col = plan.getColumns().get(i);
                System.out.println("  列" + (i+1) + ": ");
                System.out.println("    - 名称: " + col.getName());
                System.out.println("    - 类型: " + col.getType());
                System.out.println("    - 长度: " + col.getLength());
                System.out.println("    - 主键: " + (col.isPrimaryKey() ? "是" : "否"));
            }
            
        } catch (SQLCompilerException e) {
            System.out.println("❌ 编译失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demonstrateSelect(SQLCompiler compiler) {
        System.out.println("=== SELECT 编译演示 ===");
        try {
            String sql = "SELECT id, name FROM users WHERE age > 18;";
            System.out.println("SQL: " + sql);
            
            SelectPlan plan = compiler.compileSelect(sql);
            System.out.println("✅ 编译成功!");
            System.out.println("计划类型: " + plan.getClass().getSimpleName());
            System.out.println("表名: " + plan.getTableName());
            System.out.println("选择列: " + plan.getColumns());
            System.out.println("过滤条件: " + plan.getFilter());
            
        } catch (SQLCompilerException e) {
            System.out.println("❌ 编译失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demonstrateInsert(SQLCompiler compiler) {
        System.out.println("=== INSERT 编译演示 ===");
        try {
            String sql = "INSERT INTO users (id, name, age) VALUES (1, 'John', 25);";
            System.out.println("SQL: " + sql);
            
            InsertPlan plan = compiler.compileInsert(sql);
            System.out.println("✅ 编译成功!");
            System.out.println("计划类型: " + plan.getClass().getSimpleName());
            System.out.println("表名: " + plan.getTableName());
            System.out.println("插入值: " + plan.getValues());
            
        } catch (SQLCompilerException e) {
            System.out.println("❌ 编译失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demonstrateDelete(SQLCompiler compiler) {
        System.out.println("=== DELETE 编译演示 ===");
        try {
            String sql = "DELETE FROM users WHERE id = 1;";
            System.out.println("SQL: " + sql);
            
            DeletePlan plan = compiler.compileDelete(sql);
            System.out.println("✅ 编译成功!");
            System.out.println("计划类型: " + plan.getClass().getSimpleName());
            System.out.println("表名: " + plan.getTableName());
            System.out.println("删除条件: " + plan.getFilter());
            
        } catch (SQLCompilerException e) {
            System.out.println("❌ 编译失败: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demonstrateErrorHandling(SQLCompiler compiler) {
        System.out.println("=== 错误处理演示 ===");
        
        // 语法错误
        try {
            String invalidSql = "SELECT FROM users;"; // 缺少列名
            compiler.compile(invalidSql);
        } catch (SQLCompilerException e) {
            System.out.println("✅ 正确捕获语法错误: " + e.getMessage());
        }
        
        // 语义错误
        try {
            String invalidSql = "SELECT * FROM nonexistent_table;";
            compiler.compile(invalidSql);
        } catch (SQLCompilerException e) {
            System.out.println("✅ 正确捕获语义错误: " + e.getMessage());
        }
        
        // 类型不匹配错误
        try {
            String selectSql = "SELECT * FROM users;";
            compiler.compileInsert(selectSql); // 期望INSERT但提供SELECT
        } catch (SQLCompilerException e) {
            System.out.println("✅ 正确捕获类型不匹配错误: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demonstrateValidation(SQLCompiler compiler) {
        System.out.println("=== SQL验证演示 ===");
        
        // 验证正确的SQL
        String validSql = "SELECT id, name FROM users WHERE age > 18;";
        ValidationResult result1 = compiler.validate(validSql);
        System.out.println("验证 '" + validSql + "'");
        System.out.println("结果: " + (result1.isSuccess() ? "✅ 通过" : "❌ 失败"));
        System.out.println("消息: " + result1.getMessage());
        
        // 验证错误的SQL
        String invalidSql = "SELECT FROM users;";
        ValidationResult result2 = compiler.validate(invalidSql);
        System.out.println("\n验证 '" + invalidSql + "'");
        System.out.println("结果: " + (result2.isSuccess() ? "✅ 通过" : "❌ 失败"));
        System.out.println("消息: " + result2.getMessage());
        System.out.println();
    }
    
    /**
     * 实际执行CREATE TABLE操作以注册表到目录中
     */
    private static void executeCreateTable(SQLCompiler compiler, String sql) {
        try {
            CreateTablePlan plan = compiler.compileCreateTable(sql);
            
            // 使用与Main.java相同的方式执行CREATE TABLE
            parser.SQLLexer lexer = new parser.SQLLexer(sql);
            parser.SQLParser parser = new parser.SQLParser(lexer.getAllTokens());
            parser.ASTNode ast = parser.parse();
            
            if (ast != null) {
                PlanGenerationResult planResult = compiler.getSemanticAnalyzer().generatePlan(ast);
                
                if (planResult.isSuccess() && planResult.getPlan() instanceof CreateTablePlan) {
                    CreateTablePlan createPlan = (CreateTablePlan) planResult.getPlan();
                    
                    // 将Column转换为ColumnDefinition并创建TableSchema
                    executor.common.TableSchema.Builder builder = new executor.common.TableSchema.Builder()
                            .tableName(createPlan.getTableName());
                    
                    for (common.Column col : createPlan.getColumns()) {
                        executor.common.ColumnType columnType = convertStringToColumnType(col.getType());
                        builder.addColumn(col.getName(), columnType, col.getLength(), col.isPrimaryKey());
                    }
                    
                    executor.common.TableSchema schema = builder.build();
                    compiler.getSemanticAnalyzer().getCatalog().registerTable(createPlan.getTableName(), schema);
                    
                    System.out.println("✅ 表 " + createPlan.getTableName() + " 已成功注册到系统目录");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ 执行CREATE TABLE失败: " + e.getMessage());
        }
    }
    
    /**
     * 将字符串类型转换为ColumnType
     */
    private static executor.common.ColumnType convertStringToColumnType(String typeStr) {
        if (typeStr == null) {
            return executor.common.ColumnType.VARCHAR;
        }
        
        switch (typeStr.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return executor.common.ColumnType.INT;
            case "FLOAT":
            case "DOUBLE":
                return executor.common.ColumnType.FLOAT;
            case "VARCHAR":
            case "STRING":
            default:
                return executor.common.ColumnType.VARCHAR;
        }
    }
}
