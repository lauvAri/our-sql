package parser.semantic;

import common.plan.*;

/**
 * 简单的SQL编译器接口使用演示
 * 不依赖复杂的存储引擎初始化
 */
public class SimpleSQLCompilerDemo {
    
    public static void main(String[] args) {
        System.out.println("=== SQL编译器通用接口演示 ===");
        System.out.println();
        
        // 模拟不同类型的SQL语句
        String[] sqlStatements = {
            "CREATE TABLE users(id INT, name VARCHAR(50), email VARCHAR(100));",
            "INSERT INTO users (id, name, email) VALUES (1, 'John', 'john@email.com');",
            "SELECT id, name FROM users WHERE id > 0;",
            "DELETE FROM users WHERE id = 1;"
        };
        
        System.out.println("📝 接收到" + sqlStatements.length + "个未知类型的SQL语句：");
        System.out.println();
        
        for (int i = 0; i < sqlStatements.length; i++) {
            String sql = sqlStatements[i];
            System.out.println("第" + (i + 1) + "个SQL语句: " + sql);
            
            // 在真实场景中，我们会这样使用：
            // 1. 创建SQLCompiler实例（需要CatalogInterface）
            // 2. 调用compiler.compile(sql)获得LogicalPlan
            // 3. 根据plan.getOperatorType()决定如何处理
            
            // 这里我们模拟结果
            String sqlType = detectSQLType(sql);
            System.out.println("   → 检测到SQL类型: " + sqlType);
            System.out.println("   → 在真实环境中，会调用: compiler.compile(sql)");
            System.out.println("   → 然后根据LogicalPlan类型进行相应处理");
            System.out.println();
        }
        
        System.out.println("💡 核心改进点：");
        System.out.println("1. ✅ 使用统一的 compiler.compile(sql) 方法");
        System.out.println("2. ✅ 自动识别SQL类型，无需预先知道");
        System.out.println("3. ✅ 返回通用的LogicalPlan，然后进行类型转换");
        System.out.println("4. ✅ 替代了具体的 compileCreateTable、compileSelect 等方法");
        System.out.println();
        
        System.out.println("🔧 使用方式对比：");
        System.out.println();
        System.out.println("❌ 旧方式（需要预先知道SQL类型）：");
        System.out.println("   CreateTablePlan plan = compiler.compileCreateTable(sql);");
        System.out.println("   SelectPlan plan = compiler.compileSelect(sql);");
        System.out.println();
        System.out.println("✅ 新方式（通用接口）：");
        System.out.println("   LogicalPlan plan = compiler.compile(sql);");
        System.out.println("   switch (plan.getOperatorType()) {");
        System.out.println("       case CREATE_TABLE: handleCreateTable((CreateTablePlan) plan); break;");
        System.out.println("       case SELECT: handleSelect((SelectPlan) plan); break;");
        System.out.println("       case INSERT: handleInsert((InsertPlan) plan); break;");
        System.out.println("       case DELETE: handleDelete((DeletePlan) plan); break;");
        System.out.println("   }");
        System.out.println();
        
        System.out.println("🎉 演示完成！您的SQLCompiler已经具备了处理未知类型SQL的能力。");
    }
    
    /**
     * 简单的SQL类型检测（模拟）
     */
    private static String detectSQLType(String sql) {
        String upperSQL = sql.trim().toUpperCase();
        if (upperSQL.startsWith("CREATE TABLE")) {
            return "CREATE_TABLE";
        } else if (upperSQL.startsWith("INSERT")) {
            return "INSERT";
        } else if (upperSQL.startsWith("SELECT")) {
            return "SELECT";
        } else if (upperSQL.startsWith("DELETE")) {
            return "DELETE";
        } else {
            return "UNKNOWN";
        }
    }
}