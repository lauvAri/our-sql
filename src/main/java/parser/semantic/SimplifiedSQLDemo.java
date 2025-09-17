package parser.semantic;

import common.plan.*;
import java.util.*;

/**
 * 完整的SQL编译器演示
 * 直接调用SQLCompiler.compile()方法展示详细的编译过程
 */
public class SimplifiedSQLDemo {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 完整SQL编译器演示（集成详细编译过程） ===\n");
            
            // 使用增强的模拟目录适配器
            EnhancedMockCatalogAdapter mockCatalog = new EnhancedMockCatalogAdapter();
            SQLCompiler compiler = new SQLCompiler(mockCatalog);
            
            // 测试用例：包括正确和错误的SQL语句
            String[] testSQLs = {
                // 正确的SQL语句
                "CREATE TABLE users(id INT, username VARCHAR(50), email VARCHAR(100));",
                "INSERT INTO users (id, username, email) VALUES (1, 'john_doe', 'john@example.com');",
                "SELECT username, email FROM users WHERE id > 0;",
                "SELECT * FROM users;",
                "SELECT * FROM users WHERE id = 1;",
                "SELECT * FROM users ORDER BY username ASC;",
                "SELECT * FROM users ORDER BY id DESC LIMIT 10;",
                "UPDATE users SET email = 'john.doe@newdomain.com' WHERE id = 1;",
                "DELETE FROM users WHERE id = 1;",
                
                // 错误测试用例
                "CREATE TABLE users(id INT, username VARCHAR(50), email VARCHAR(100))",  // 缺分号
                "SELECT * FROM nonexistent_table;",  // 表不存在
                "SELECT nonexistent_column FROM users;",  // 列不存在
                "INSERT INTO users (id, username) VALUES (1);",  // 列数不匹配
                "SELECT * FROM users WHERE invalidcolumn = 1;"  // 无效列名
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
                    // 直接调用SQLCompiler的compile方法，它会自动输出详细的编译过程
                    LogicalPlan plan = compiler.compile(sql);
                    
                    if (plan != null) {
                        System.out.println("🎉 编译完成！最终执行计划类型: " + plan.getOperatorType());
                        
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
                            System.out.println("✅ 表已成功注册到系统目录");
                        }
                    }
                    
                } catch (SQLCompilerException e) {
                    System.out.println("❌ 编译失败: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("❌ 编译过程异常: " + e.getMessage());
                    e.printStackTrace();
                }
                
                System.out.println("\n");
            }
            
            System.out.println("\n🎯 SQL编译器功能总结:");
            System.out.println("✅ 词法分析: 输出Token[种别码, 词素值, 行号, 列号]格式");
            System.out.println("✅ 语法分析: 输出四元式[步骤，[语法栈]，（输入串），表达式]格式，包含错误处理");
            System.out.println("✅ 语义分析: 输出四元式[op, arg1, arg2, result]格式");
            System.out.println("✅ 执行计划: 生成完整的LogicalPlan对象");
            System.out.println("✅ SELECT *展开: 自动将*展开为实际列名");
            System.out.println("✅ ORDER BY/LIMIT: 支持排序和限制功能");
            System.out.println("✅ 错误处理: 精确的错误定位和详细报告");
            
            System.out.println("\n� 使用方式:");
            System.out.println("现在执行器只需调用 compiler.compile(sql) 即可看到完整的编译过程！");
            
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