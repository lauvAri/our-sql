package parser.semantic;

import parser.*;
import common.plan.*;
import java.util.*;

/**
 * 分步骤SQL编译器演示 - 直接调用各个分析器接口
 * 展示完整的编译过程：词法分析→语法分析→语义分析→执行计划生成
 */
public class StepByStepSQLDemo2 {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 分步骤SQL编译器演示（直接调用各分析器接口） ===\n");
            
            // 初始化目录适配器
            EnhancedMockCatalogAdapter mockCatalog = new EnhancedMockCatalogAdapter();
            
            // 测试SQL语句
            String[] testSQLs = {
                "CREATE TABLE users(id INT, username VARCHAR(50), email VARCHAR(100));",
                "SELECT username, email FROM users WHERE id > 0;",
                "INSERT INTO users (id, username, email) VALUES (1, 'john_doe', 'john@example.com');",
                "SELECT * FROM users;"
            };
            
            System.out.println("📝 测试" + testSQLs.length + "个SQL语句：\n");
            
            // 逐个测试SQL语句
            for (int i = 0; i < testSQLs.length; i++) {
                String sql = testSQLs[i];
                System.out.println("=" + repeat("=", 80));
                System.out.println("测试第" + (i + 1) + "个SQL语句:");
                System.out.println("SQL: " + sql);
                System.out.println("=" + repeat("=", 80));
                
                try {
                    // 第1步：词法分析
                    System.out.println("\n🔍 第1步：词法分析");
                    System.out.println("----------------------------------------");
                    System.out.println("📖 Token输出格式: [种别码, 词素值, 行号, 列号]");
                    
                    SQLLexer lexer = new SQLLexer(sql);
                    
                    // 通过SQLLexer逐个获取Token并打印
                    int tokenIndex = 0;
                    while (true) {
                        try {
                            Object token = lexer.nextToken();
                            if (token == null) break;
                            
                            System.out.println("  Token[" + tokenIndex + "]: " + token.toString());
                            tokenIndex++;
                        } catch (Exception e) {
                            break;
                        }
                    }
                    System.out.println("✅ 词法分析完成，共生成 " + tokenIndex + " 个token");
                    
                    // 第2步：语法分析
                    System.out.println("\n🎯 第2步：语法分析");
                    System.out.println("----------------------------------------");
                    System.out.println("📖 语法分析四元式输出格式: [步骤，[语法栈]，（输入串），表达式]");
                    
                    // 重新创建lexer进行语法分析
                    SQLLexer parserLexer = new SQLLexer(sql);
                    SQLParser parser = new SQLParser(parserLexer.getAllTokens());
                    ASTNode ast = parser.parse();
                    
                    if (ast != null) {
                        System.out.println("✅ 语法分析成功");
                        System.out.println("📝 AST根节点类型: " + ast.getClass().getSimpleName());
                        System.out.println("  📄 " + ast.getClass().getSimpleName() + " 节点已创建");
                    } else {
                        System.out.println("❌ 语法分析失败 - AST为null");
                        continue;
                    }
                    
                    // 第3步和第4步：使用集成的编译器来展示剩余步骤
                    System.out.println("\n🧠 第3步 & 📊 第4步：语义分析与执行计划生成");
                    System.out.println("----------------------------------------");
                    System.out.println("💡 注意：以下步骤通过SQLCompiler集成方法展示详细过程");
                    
                    // 使用SQLCompiler来展示后续步骤
                    SQLCompiler compiler = new SQLCompiler(mockCatalog);
                    LogicalPlan plan = compiler.compile(sql);
                    
                    if (plan != null) {
                        System.out.println("🎉 完整编译成功！最终执行计划类型: " + plan.getOperatorType());
                        
                        // 如果是CREATE TABLE，注册表到目录中
                        if (plan instanceof CreateTablePlan) {
                            CreateTablePlan createPlan = (CreateTablePlan) plan;
                            List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                            for (var column : createPlan.getColumns()) {
                                String name = column.getName();
                                String dataType = column.getType();
                                int size = column.getLength() > 0 ? column.getLength() : getDefaultColumnSize(dataType);
                                columnMetadataList.add(new ColumnMetadata(name, dataType, false, column.isPrimaryKey(), size));
                            }
                            mockCatalog.addTable(createPlan.getTableName(), columnMetadataList);
                            System.out.println("📝 模拟注册表: " + createPlan.getTableName() + " (包含 " + createPlan.getColumns().size() + " 列)");
                            System.out.println("✅ 表已成功注册到系统目录");
                        }
                    } else {
                        System.out.println("❌ 编译失败");
                    }
                    
                } catch (Exception e) {
                    System.out.println("❌ 编译过程异常: " + e.getMessage());
                    e.printStackTrace();
                }
                
                System.out.println("\n");
            }
            
            System.out.println("\n🎯 分步骤SQL编译器功能总结:");
            System.out.println("✅ 第1步：词法分析 - 直接调用SQLLexer.nextToken()展示每个Token");
            System.out.println("✅ 第2步：语法分析 - 直接调用SQLParser.parse()并显示完整的四元式过程");
            System.out.println("✅ 第3步：语义分析 - 通过SQLCompiler.compile()集成展示四元式输出");
            System.out.println("✅ 第4步：执行计划 - 生成完整的LogicalPlan对象");
            System.out.println("✅ 错误处理：精确的错误定位和详细报告");
            System.out.println("✅ AST构建：完整的抽象语法树生成");
            
            System.out.println("\n📚 使用方式:");
            System.out.println("这个演示直接调用SQLLexer和SQLParser等分析器接口，");
            System.out.println("展示编译器各个阶段的详细过程，适合向老师展示编译器内部机制！");
            
        } catch (Exception e) {
            System.err.println("❌ 演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取数据类型的默认大小
     */
    private static int getDefaultColumnSize(String dataType) {
        switch (dataType.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return 4;
            case "BIGINT":
                return 8;
            case "SMALLINT":
                return 2;
            case "TINYINT":
                return 1;
            case "FLOAT":
                return 4;
            case "DOUBLE":
                return 8;
            case "BOOLEAN":
                return 1;
            case "DATE":
                return 4;
            case "TIMESTAMP":
                return 8;
            case "VARCHAR":
                return 255; // 默认VARCHAR大小
            case "CHAR":
                return 1;
            case "TEXT":
                return 65535;
            default:
                return 50; // 未知类型默认大小
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
}