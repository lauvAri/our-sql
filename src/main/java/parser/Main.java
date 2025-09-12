package parser;
import semantic.*;
import common.plan.*;
import executor.common.TableSchema;
import executor.common.ColumnType;
import executor.systemCatalog.CatalogManager;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import storage.service.StorageService;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        try {
            // 创建存储服务和存储引擎
            String dbFileName = "test_database.db";
            String idxFileName = "test_index.idx";
            StorageService storageService = new StorageService(dbFileName, idxFileName);
            StorageEngine storageEngine = new StorageEngineImpl(storageService);
            
            // 创建数据库目录和增强语义分析器（使用适配器模式）
            CatalogManager catalogManager = new CatalogManager(storageEngine);
            CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);
            EnhancedSemanticAnalyzer analyzer = new EnhancedSemanticAnalyzer(catalogAdapter);
            
            System.out.println("=== SQL编译器初始化完成 ===");
            System.out.println("使用适配器模式集成executor模块的CatalogManager");
            System.out.println("存储引擎: " + storageEngine.getClass().getSimpleName());
            System.out.println("数据库文件: " + dbFileName);
            System.out.println("索引文件: " + idxFileName);
            
            // 先创建测试表
            System.out.println("\n=== 步骤1: 创建测试表 ===");
            String createUsersTable = "CREATE TABLE users (id INT, name VARCHAR, age INT);";
            testComprehensiveAnalysis(createUsersTable, analyzer);
            // 执行CREATE TABLE计划
            executeCreateTableIfSuccess(createUsersTable, analyzer);
            
            String createOrdersTable = "CREATE TABLE orders (id INT, customer_id INT, total FLOAT);";
            testComprehensiveAnalysis(createOrdersTable, analyzer);
            executeCreateTableIfSuccess(createOrdersTable, analyzer);
            
            // 现在测试查询操作
            System.out.println("\n=== 步骤2: 测试 SELECT 语句完整分析 ===");
            String sql1 = "SELECT id, name FROM users WHERE age > 18;";
            testComprehensiveAnalysis(sql1, analyzer);
            
            // 测试不存在的表
            System.out.println("\n=== 步骤3: 测试不存在的表 ===");
            String sql2 = "SELECT * FROM nonexistent;";
            testComprehensiveAnalysis(sql2, analyzer);
            
            // 测试不存在的列
            System.out.println("\n=== 步骤4: 测试不存在的列 ===");
            String sql3 = "SELECT id, nonexistent_column FROM users;";
            testComprehensiveAnalysis(sql3, analyzer);
            
            // 测试INSERT语句
            System.out.println("\n=== 测试 INSERT 语句 ===");
            String sql5 = "INSERT INTO users (id, name, age) VALUES (1, 'John', 25);";
            testComprehensiveAnalysis(sql5, analyzer);
            
            // 测试DELETE语句
            System.out.println("\n=== 测试 DELETE 语句 ===");
            String sql6 = "DELETE FROM users WHERE id = 1;";
            testComprehensiveAnalysis(sql6, analyzer);
            
            // 测试执行计划的不同输出格式
            System.out.println("\n=== 测试执行计划输出格式 ===");
            testPlanFormats(sql1, analyzer);
            
            // 测试LogicalPlan子类对象创建
            System.out.println("\n=== 验证LogicalPlan子类对象创建 ===");
            testLogicalPlanObjects(analyzer);
            
            System.out.println("\n=== SQL编译器测试完成 ===");
            System.out.println("所有功能均已通过适配器模式与executor模块集成");
            
        } catch (Exception e) {
            System.err.println("初始化或运行过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void testComprehensiveAnalysis(String sql, EnhancedSemanticAnalyzer analyzer) {
        System.out.println("SQL: " + sql);
        
        // 词法分析
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();
        
        // 语法分析
        SQLParser parser = new SQLParser(tokens);
        ASTNode ast = parser.parse();
        
        if (ast != null) {
            System.out.println("AST: " + ast.toString());
            
            // 综合分析
            EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult result = analyzer.analyze(ast);
            System.out.println(result.getFormattedResult());
        } else {
            System.out.println("语法分析失败");
        }
    }
    
    public static void testPlanFormats(String sql, EnhancedSemanticAnalyzer analyzer) {
        System.out.println("SQL: " + sql);
        
        // 词法分析
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();
        
        // 语法分析
        SQLParser parser = new SQLParser(tokens);
        ASTNode ast = parser.parse();
        
        if (ast != null) {
            // 生成执行计划
            PlanGenerationResult planResult = analyzer.generatePlan(ast);
            
            if (planResult.isSuccess() && planResult.getPlan() != null) {
                // 验证LogicalPlan对象类型
                System.out.println("\n--- LogicalPlan对象验证 ---");
                System.out.println(PlanGenerator.validatePlanType(planResult.getPlan()));
                
                System.out.println("\n--- 树形格式 ---");
                System.out.println(PlanFormatter.format(planResult.getPlan(), PlanFormatter.OutputFormat.TREE));
                
                System.out.println("\n--- JSON格式 ---");
                System.out.println(PlanFormatter.format(planResult.getPlan(), PlanFormatter.OutputFormat.JSON));
                
                System.out.println("\n--- S表达式格式 ---");
                System.out.println(PlanFormatter.format(planResult.getPlan(), PlanFormatter.OutputFormat.S_EXPR));
            } else {
                System.out.println("执行计划生成失败");
                if (planResult.hasErrors()) {
                    for (SemanticError error : planResult.getErrors()) {
                        System.out.println("错误: " + error.toString());
                    }
                }
            }
        }
    }
    
    public static void testLogicalPlanObjects(EnhancedSemanticAnalyzer analyzer) {
        System.out.println("验证不同SQL语句生成的LogicalPlan子类对象...\n");
        
        String[] testSqls = {
            "SELECT id, name FROM users WHERE age > 18;",
            "CREATE TABLE test_table (id INT, name VARCHAR);",
            "INSERT INTO users (id, name, age) VALUES (1, 'Test', 25);",
            "DELETE FROM users WHERE id = 1;"
        };
        
        for (String sql : testSqls) {
            System.out.println("测试SQL: " + sql);
            
            try {
                // 词法分析
                SQLLexer lexer = new SQLLexer(sql);
                List<Token> tokens = lexer.getAllTokens();
                
                // 语法分析
                SQLParser parser = new SQLParser(tokens);
                ASTNode ast = parser.parse();
                
                if (ast != null) {
                    // 生成执行计划
                    PlanGenerationResult planResult = analyzer.generatePlan(ast);
                    
                    if (planResult.isSuccess() && planResult.getPlan() != null) {
                        System.out.println(PlanGenerator.validatePlanType(planResult.getPlan()));
                    } else {
                        System.out.println("❌ 执行计划生成失败");
                        if (planResult.hasErrors()) {
                            for (SemanticError error : planResult.getErrors()) {
                                System.out.println("  错误: " + error.toString());
                            }
                        }
                    }
                } else {
                    System.out.println("❌ 语法分析失败");
                }
            } catch (Exception e) {
                System.out.println("❌ 异常: " + e.getMessage());
            }
            
            System.out.println("---");
        }
    }
    
    /**
     * 如果CREATE TABLE语义分析成功，则实际执行创建表的操作
     */
    public static void executeCreateTableIfSuccess(String sql, EnhancedSemanticAnalyzer analyzer) {
        try {
            // 词法分析
            SQLLexer lexer = new SQLLexer(sql);
            List<Token> tokens = lexer.getAllTokens();
            
            // 语法分析
            SQLParser parser = new SQLParser(tokens);
            ASTNode ast = parser.parse();
            
            if (ast != null) {
                // 生成执行计划
                PlanGenerationResult planResult = analyzer.generatePlan(ast);
                
                if (planResult.isSuccess() && planResult.getPlan() != null) {
                    // 如果是CREATE TABLE操作，实际执行它
                    if (planResult.getPlan() instanceof CreateTablePlan) {
                        CreateTablePlan createPlan = (CreateTablePlan) planResult.getPlan();
                        System.out.println("✅ 正在执行CREATE TABLE: " + createPlan.getTableName());
                        
                        // 实际执行CREATE TABLE操作 - 通过转换和注册
                        // 1. 将Column转换为ColumnDefinition并创建TableSchema
                        TableSchema.Builder builder = new TableSchema.Builder()
                                .tableName(createPlan.getTableName());
                        
                        // 将common.Column转换为executor.common.ColumnDefinition
                        for (common.Column col : createPlan.getColumns()) {
                            ColumnType columnType = convertStringToColumnType(col.getType());
                            builder.addColumn(col.getName(), columnType, col.getLength(), col.isPrimaryKey());
                        }
                        
                        TableSchema schema = builder.build();
                        
                        // 2. 注册到系统目录
                        analyzer.getCatalog().registerTable(createPlan.getTableName(), schema);
                        
                        System.out.println("   ✅ 表 " + createPlan.getTableName() + " 创建并注册完成");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("执行CREATE TABLE时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 将字符串类型转换为ColumnType
     */
    private static ColumnType convertStringToColumnType(String typeStr) {
        if (typeStr == null) {
            return ColumnType.VARCHAR;
        }
        
        switch (typeStr.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return ColumnType.INT;
            case "FLOAT":
            case "DOUBLE":
                return ColumnType.FLOAT;
            case "VARCHAR":
            case "CHAR":
            case "STRING":
                return ColumnType.VARCHAR;
            case "BOOLEAN":
            case "BOOL":
                return ColumnType.BOOLEAN;
            case "TIMESTAMP":
            case "DATETIME":
                return ColumnType.TIMESTAMP;
            default:
                return ColumnType.VARCHAR;
        }
    }
}
