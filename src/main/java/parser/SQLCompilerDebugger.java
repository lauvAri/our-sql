package parser;

import common.plan.*;
import executor.systemCatalog.CatalogManager;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import storage.service.StorageService;
import parser.semantic.*;

/**
 * SQL编译器调试版本
 */
public class SQLCompilerDebugger {
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
            
            System.out.println("=== SQL编译器调试模式 ===");
            
            // 测试SQL
            String createSQL = "CREATE TABLE student(id INT, name VARCHAR(50), age INT);";
            System.out.println("测试SQL: " + createSQL);
            
            try {
                // 步骤1：词法分析
                System.out.println("\n1. 词法分析：");
                SQLLexer lexer = new SQLLexer(createSQL);
                java.util.List<Token> tokens = lexer.getAllTokens();
                for (int i = 0; i < tokens.size(); i++) {
                    System.out.printf("  %d: %s\n", i, tokens.get(i));
                }
                
                // 步骤2：语法分析
                System.out.println("\n2. 语法分析：");
                SQLParser parser = new SQLParser(tokens);
                ASTNode ast = parser.parse();
                if (ast != null) {
                    System.out.println("  ✅ 语法分析成功");
                    System.out.println("  AST类型: " + ast.getType());
                    System.out.println("  AST内容: " + ast.toString());
                } else {
                    System.out.println("  ❌ 语法分析失败");
                    System.out.println("  语法分析输出：");
                    System.out.println(parser.getOutput());
                    return;
                }
                
                // 步骤3：语义分析
                System.out.println("\n3. 语义分析：");
                EnhancedSemanticAnalyzer semanticAnalyzer = compiler.getSemanticAnalyzer();
                EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult analysisResult = semanticAnalyzer.analyze(ast);
                
                if (analysisResult.isFullySuccessful()) {
                    System.out.println("  ✅ 语义分析成功");
                } else {
                    System.out.println("  ❌ 语义分析失败");
                    if (analysisResult.getSemanticResult().getErrors() != null) {
                        System.out.println("  错误: " + analysisResult.getSemanticResult().getErrors());
                    }
                    return;
                }
                
                // 步骤4：执行计划生成
                System.out.println("\n4. 执行计划生成：");
                PlanGenerationResult planResult = semanticAnalyzer.generatePlan(ast);
                
                if (planResult.isSuccess()) {
                    System.out.println("  ✅ 执行计划生成成功");
                    LogicalPlan plan = planResult.getPlan();
                    System.out.println("  计划类型: " + plan.getClass().getSimpleName());
                    System.out.println("  计划内容: " + plan.toString());
                    
                    if (plan instanceof CreateTablePlan) {
                        CreateTablePlan createPlan = (CreateTablePlan) plan;
                        System.out.println("  表名: " + createPlan.getTableName());
                        System.out.println("  列数: " + createPlan.getColumns().size());
                        for (int i = 0; i < createPlan.getColumns().size(); i++) {
                            common.Column col = createPlan.getColumns().get(i);
                            System.out.println("    列" + (i+1) + ": " + col.getName() + 
                                " (" + col.getType() + ", 长度:" + col.getLength() + 
                                ", 主键:" + (col.isPrimaryKey() ? "是" : "否") + ")");
                        }
                    }
                } else {
                    System.out.println("  ❌ 执行计划生成失败");
                    System.out.println("  错误: " + planResult.getErrors());
                }
                
            } catch (Exception e) {
                System.err.println("调试过程中发生错误: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("❌ 初始化错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
