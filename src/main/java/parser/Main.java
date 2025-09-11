package parser;
import semantic.*;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        // 创建数据库目录和增强语义分析器
        Catalog catalog = new Catalog();
        EnhancedSemanticAnalyzer analyzer = new EnhancedSemanticAnalyzer(catalog);
        
        System.out.println("=== 数据库目录初始状态 ===");
        System.out.println(catalog.getStatus());
        
        // 测试SELECT语句的完整分析
        System.out.println("=== 测试 SELECT 语句完整分析 ===");
        String sql1 = "SELECT id, name FROM users WHERE age > 18;";
        testComprehensiveAnalysis(sql1, analyzer);
        
        // 测试不存在的表
        System.out.println("\n=== 测试不存在的表 ===");
        String sql2 = "SELECT * FROM nonexistent;";
        testComprehensiveAnalysis(sql2, analyzer);
        
        // 测试不存在的列
        System.out.println("\n=== 测试不存在的列 ===");
        String sql3 = "SELECT id, nonexistent_column FROM users;";
        testComprehensiveAnalysis(sql3, analyzer);
        
        // 测试CREATE TABLE语句
        System.out.println("\n=== 测试 CREATE TABLE 语句 ===");
        String sql4 = "CREATE TABLE orders (id INT, customer_id INT, total FLOAT);";
        testComprehensiveAnalysis(sql4, analyzer);
        
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
        
        System.out.println("\n=== 最终数据库目录状态 ===");
        System.out.println(catalog.getStatus());
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
}
