package parser.semantic;

import common.plan.*;

/**
 * SQL处理器工具类
 * 演示如何在真实场景中使用SQLCompiler的通用接口
 * 
 * @author SQL编译器团队
 * @version 1.0
 */
public class SQLProcessor {
    
    private final SQLCompiler compiler;
    
    public SQLProcessor(SQLCompiler compiler) {
        this.compiler = compiler;
    }
    
    /**
     * 处理任意SQL语句的通用方法
     * 自动识别SQL类型并执行相应的处理逻辑
     * 
     * @param sql SQL语句
     * @return 处理结果描述
     * @throws SQLCompilerException 编译异常
     */
    public String processSQL(String sql) throws SQLCompilerException {
        // 使用通用编译接口
        LogicalPlan plan = compiler.compile(sql);
        
        // 根据计划类型进行相应处理
        switch (plan.getOperatorType()) {
            case CREATE_TABLE:
                return handleCreateTable((CreateTablePlan) plan);
                
            case SELECT:
                return handleSelect((SelectPlan) plan);
                
            case INSERT:
                return handleInsert((InsertPlan) plan);
                
            case DELETE:
                return handleDelete((DeletePlan) plan);
                
            default:
                return "不支持的SQL操作类型: " + plan.getOperatorType();
        }
    }
    
    /**
     * 处理CREATE TABLE语句
     */
    private String handleCreateTable(CreateTablePlan plan) {
        StringBuilder result = new StringBuilder();
        result.append("创建表操作:\n");
        result.append("  表名: ").append(plan.getTableName()).append("\n");
        result.append("  列信息:\n");
        
        for (int i = 0; i < plan.getColumns().size(); i++) {
            common.Column col = plan.getColumns().get(i);
            result.append("    ").append(i + 1).append(". ")
                  .append(col.getName()).append(" (")
                  .append(col.getType()).append(", 长度: ")
                  .append(col.getLength()).append(", 主键: ")
                  .append(col.isPrimaryKey() ? "是" : "否").append(")\n");
        }
        
        return result.toString();
    }
    
    /**
     * 处理SELECT语句
     */
    private String handleSelect(SelectPlan plan) {
        StringBuilder result = new StringBuilder();
        result.append("查询操作:\n");
        result.append("  表名: ").append(plan.getTableName()).append("\n");
        result.append("  查询列: ").append(plan.getColumns()).append("\n");
        
        if (plan.getFilter() != null) {
            result.append("  WHERE条件: ").append(plan.getFilter()).append("\n");
        } else {
            result.append("  WHERE条件: 无\n");
        }
        
        return result.toString();
    }
    
    /**
     * 处理INSERT语句
     */
    private String handleInsert(InsertPlan plan) {
        StringBuilder result = new StringBuilder();
        result.append("插入操作:\n");
        result.append("  表名: ").append(plan.getTableName()).append("\n");
        result.append("  插入值: ").append(plan.getValues()).append("\n");
        
        return result.toString();
    }
    
    /**
     * 处理DELETE语句
     */
    private String handleDelete(DeletePlan plan) {
        StringBuilder result = new StringBuilder();
        result.append("删除操作:\n");
        result.append("  表名: ").append(plan.getTableName()).append("\n");
        
        if (plan.getFilter() != null) {
            result.append("  WHERE条件: ").append(plan.getFilter()).append("\n");
        } else {
            result.append("  WHERE条件: 无 (删除所有记录)\n");
        }
        
        return result.toString();
    }
    
    /**
     * 批量处理多个SQL语句
     * 
     * @param sqlStatements SQL语句数组
     * @return 每个语句的处理结果
     */
    public String[] batchProcess(String[] sqlStatements) {
        String[] results = new String[sqlStatements.length];
        
        for (int i = 0; i < sqlStatements.length; i++) {
            try {
                results[i] = "✅ " + processSQL(sqlStatements[i]);
            } catch (SQLCompilerException e) {
                results[i] = "❌ 编译失败: " + e.getMessage();
            } catch (Exception e) {
                results[i] = "❌ 处理失败: " + e.getMessage();
            }
        }
        
        return results;
    }
    
    /**
     * 验证SQL语句是否正确
     * 
     * @param sql SQL语句
     * @return 验证结果
     */
    public boolean validateSQL(String sql) {
        try {
            compiler.compile(sql);
            return true;
        } catch (SQLCompilerException e) {
            return false;
        }
    }
    
    /**
     * 获取SQL语句的类型（不执行编译）
     * 
     * @param sql SQL语句
     * @return SQL类型字符串
     */
    public String getSQLType(String sql) {
        try {
            LogicalPlan plan = compiler.compile(sql);
            return plan.getOperatorType().toString();
        } catch (SQLCompilerException e) {
            return "UNKNOWN";
        }
    }
}