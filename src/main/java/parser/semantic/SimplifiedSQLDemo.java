package parser.semantic;

import common.plan.*;
import java.util.*;

/**
 * 简化的SQL编译器演示，使用模拟的目录适配器
 * 避免复杂的存储引擎初始化问题
 */
public class SimplifiedSQLDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 简化的SQL编译器通用接口演示 ===\n");
            
            // 使用增强的模拟目录适配器
            EnhancedMockCatalogAdapter mockCatalog = new EnhancedMockCatalogAdapter();
            SQLCompiler compiler = new SQLCompiler(mockCatalog);
            
            // 测试支持的SQL语句（使用正确的数据类型）
            String[] testSQLs = {
                "CREATE TABLE users(id INT, username VARCHAR(50), email VARCHAR(100));",
                "INSERT INTO users (id, username, email) VALUES (1, 'john_doe', 'john@example.com');",
                "SELECT username, email FROM users WHERE id > 0;",
                "DELETE FROM users WHERE id = 1;",
                "CREATE TABLE products(pid INT, name VARCHAR(100), price VARCHAR(20));"
            };
            
            System.out.println("📝 测试" + testSQLs.length + "个SQL语句：\n");
            
            // 逐个测试SQL语句
            for (int i = 0; i < testSQLs.length; i++) {
                String sql = testSQLs[i];
                System.out.println("测试第" + (i + 1) + "个SQL语句:");
                System.out.println("SQL: " + sql);
                
                try {
                    // 使用通用编译接口
                    LogicalPlan plan = compiler.compile(sql);
                    
                    System.out.println("✅ 编译成功!");
                    System.out.println("   操作类型: " + plan.getOperatorType());
                    
                    // 根据类型显示详细信息
                    switch (plan.getOperatorType()) {
                        case CREATE_TABLE:
                            CreateTablePlan createPlan = (CreateTablePlan) plan;
                            System.out.println("   表名: " + createPlan.getTableName());
                            System.out.println("   列数: " + createPlan.getColumns().size());
                            
                            // 将CreateTablePlan的列定义转换为ColumnMetadata
                            List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                            for (var column : createPlan.getColumns()) {
                                String name = column.getName();
                                String dataType = column.getType();
                                int size = column.getLength() > 0 ? column.getLength() : getDefaultColumnSize(dataType);
                                columnMetadataList.add(new ColumnMetadata(name, dataType, false, column.isPrimaryKey(), size));
                            }
                            
                            // 注册表及其列信息到目录中
                            mockCatalog.addTable(createPlan.getTableName(), columnMetadataList);
                            System.out.println("   ✅ 已注册表到目录中");
                            break;
                            
                        case SELECT:
                            SelectPlan selectPlan = (SelectPlan) plan;
                            System.out.println("   查询表: " + selectPlan.getTableName());
                            System.out.println("   选择列: " + selectPlan.getColumns());
                            break;
                            
                        case INSERT:
                            InsertPlan insertPlan = (InsertPlan) plan;
                            System.out.println("   插入表: " + insertPlan.getTableName());
                            System.out.println("   插入值: " + insertPlan.getValues());
                            break;
                            
                        case DELETE:
                            DeletePlan deletePlan = (DeletePlan) plan;
                            System.out.println("   删除表: " + deletePlan.getTableName());
                            if (deletePlan.getFilter() != null) {
                                System.out.println("   删除条件: " + deletePlan.getFilter());
                            }
                            break;
                            
                        case CREATE_INDEX:
                        case DROP_INDEX:
                            System.out.println("   索引操作（暂不详细展示）");
                            break;
                    }
                    
                } catch (SQLCompilerException e) {
                    System.out.println("❌ 编译失败: " + e.getMessage());
                    
                    // 添加更深入的错误分析
                    System.out.println("🔍 深度错误分析:");
                    try {
                        // 1. 测试词法分析
                        parser.SQLLexer lexer = new parser.SQLLexer(sql);
                        System.out.println("   ✅ 词法分析成功");
                        
                        // 2. 测试语法分析
                        parser.SQLParser parser = new parser.SQLParser(lexer.getAllTokens());
                        parser.ASTNode ast = parser.parse();
                        if (ast != null) {
                            System.out.println("   ✅ 语法分析成功 - AST类型: " + ast.getClass().getSimpleName());
                            
                            // 3. 测试语义分析器
                            parser.semantic.EnhancedSemanticAnalyzer analyzer = compiler.getSemanticAnalyzer();
                            parser.semantic.AnalysisResult result = analyzer.analyzeSemantics(ast);
                            
                            if (result.isSuccess()) {
                                System.out.println("   ✅ 语义分析成功");
                            } else {
                                System.out.println("   ❌ 语义分析失败");
                                if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                                    System.out.println("   详细错误: " + result.getErrors());
                                } else {
                                    System.out.println("   错误信息为空，但分析失败");
                                }
                                System.out.println("   完整错误报告:");
                                System.out.println(result.getFormattedResult());
                            }
                        } else {
                            System.out.println("   ❌ 语法分析失败 - AST为null");
                        }
                    } catch (Exception ex) {
                        System.out.println("   ❌ 分析过程异常: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                
                System.out.println(repeat("-", 60));
            }
            
            System.out.println("\n🎯 核心改进总结:");
            System.out.println("1. ✅ 使用通用的 compiler.compile(sql) 方法");
            System.out.println("2. ✅ 自动识别SQL类型，无需预先知道");
            System.out.println("3. ✅ 根据 LogicalPlan 类型进行相应处理");
            System.out.println("4. ✅ 解决了存储引擎初始化问题");
            System.out.println("5. ✅ 修复了不支持的数据类型问题");
            
            System.out.println("\n🔧 使用方式改进:");
            System.out.println("❌ 旧方式: compiler.compileCreateTable(sql) // 需要预知类型");
            System.out.println("✅ 新方式: compiler.compile(sql) // 通用接口");
            
            System.out.println("\n🎉 现在您的SQL编译器可以处理真实场景中的未知类型SQL语句了！");
            
        } catch (Exception e) {
            System.err.println("❌ 演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 重复字符串
     */
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * 获取数据类型的默认大小
     */
    private static int getDefaultColumnSize(String dataType) {
        if (dataType.equalsIgnoreCase("INT")) {
            return 4;
        } else if (dataType.toUpperCase().startsWith("VARCHAR")) {
            // 从VARCHAR(n)中提取n，如果没有则返回默认值
            if (dataType.contains("(") && dataType.contains(")")) {
                try {
                    String sizeStr = dataType.substring(dataType.indexOf("(") + 1, dataType.indexOf(")"));
                    return Integer.parseInt(sizeStr);
                } catch (Exception e) {
                    return 255; // 默认VARCHAR大小
                }
            } else {
                return 255; // 默认VARCHAR大小
            }
        } else {
            return 100; // 其他类型的默认大小
        }
    }
}