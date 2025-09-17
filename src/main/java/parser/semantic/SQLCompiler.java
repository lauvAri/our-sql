package parser.semantic;

import parser.SQLLexer;
import parser.SQLParser;
import parser.ASTNode;
import parser.ASTFieldAccessor;
import common.plan.*;
import java.util.*;

/**
 * SQL编译器统一接口
 * 提供将SQL语句转换为LogicalPlan子类的核心功能
 * 
 * @author SQL编译器团队
 * @version 1.0
 */
public class SQLCompiler {
    
    private final EnhancedSemanticAnalyzer semanticAnalyzer;
    
    /**
     * 构造函数
     * @param catalogInterface 系统目录接口
     */
    public SQLCompiler(CatalogInterface catalogInterface) {
        this.semanticAnalyzer = new EnhancedSemanticAnalyzer(catalogInterface);
    }
    
    /**
     * 核心接口：将SQL语句编译为LogicalPlan子类对象
     * 自动输出详细的编译过程信息
     * @param sql SQL语句
     * @return LogicalPlan子类对象，如果编译失败则返回null
     * @throws SQLCompilerException 编译过程中的异常
     */
    public LogicalPlan compile(String sql) throws SQLCompilerException {
        System.out.println("\n🔍 开始详细编译过程...\n");
        
        try {
            // ============ 第一阶段：词法分析 ============
            System.out.println("📊 阶段1: 词法分析");
            System.out.println(repeat("-", 40));
            
            SQLLexer lexer = new SQLLexer(sql);
            var tokens = lexer.getAllTokens();
            
            System.out.println("🎯 Token输出格式: [种别码, 词素值, 行号, 列号]");
            for (int i = 0; i < tokens.size(); i++) {
                // 使用Token的toString方法，它已经是[种别码, 词素值, 行号, 列号]格式
                Object token = tokens.get(i);
                System.out.println("  Token[" + i + "]: " + token.toString());
            }
            System.out.println("✅ 词法分析完成，共生成 " + tokens.size() + " 个token\n");
            
            // ============ 第二阶段：语法分析 ============
            System.out.println("📊 阶段2: 语法分析");
            System.out.println(repeat("-", 40));
            
            SQLParser parser = new SQLParser(tokens);
            ASTNode ast = parser.parse();
            
            if (ast == null) {
                System.out.println("❌ 语法分析失败 - AST为null");
                throw new SQLCompilerException("语法分析失败：无法解析SQL语句", sql);
            }
            
            System.out.println("✅ 语法分析成功");
            System.out.println("🌳 AST根节点类型: " + ast.getClass().getSimpleName());
            displayASTStructure(ast);
            System.out.println();
            
            // ============ 第三阶段：语义分析 ============
            System.out.println("📊 阶段3: 语义分析");
            System.out.println(repeat("-", 40));
            
            EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult analysisResult = semanticAnalyzer.analyze(ast);
            
            if (!analysisResult.isFullySuccessful()) {
                System.out.println("❌ 语义分析失败");
                System.out.println("🚨 错误输出格式: [错误类型, 位置, 原因]");
                if (analysisResult.getSemanticResult().getErrors() != null) {
                    for (parser.semantic.SemanticError error : analysisResult.getSemanticResult().getErrors()) {
                        System.out.println("  错误: " + error.toString());
                    }
                }
                System.out.println("\n📋 完整分析报告:");
                System.out.println(analysisResult.getSemanticResult().getFormattedResult());
                
                String errorMsg = analysisResult.getSemanticResult().getErrors() != null ? 
                    analysisResult.getSemanticResult().getErrors().toString() : "语义分析失败";
                throw new SQLCompilerException("语义分析失败：" + errorMsg, sql);
            }
            
            System.out.println("✅ 语义分析成功");
            System.out.println("🎯 四元式输出格式: [op, arg1, arg2, result]");
            
            // 输出语义分析产生的四元式
            if (analysisResult.getSemanticResult().getQuadruples() != null) {
                List<Quadruple> quadruples = analysisResult.getSemanticResult().getQuadruples();
                for (int i = 0; i < quadruples.size(); i++) {
                    Quadruple quad = quadruples.get(i);
                    System.out.println("  四元式[" + (i+1) + "]: " + quad.toString());
                }
            }
            System.out.println();
            
            // ============ 第四阶段：执行计划生成 ============
            System.out.println("📊 阶段4: 执行计划生成");
            System.out.println(repeat("-", 40));
            
            PlanGenerationResult planResult = semanticAnalyzer.generatePlan(ast);
            if (!planResult.isSuccess()) {
                System.out.println("❌ 执行计划生成失败: " + planResult.getErrors());
                throw new SQLCompilerException("执行计划生成失败：" + planResult.getErrors(), sql);
            }
            
            System.out.println("✅ 执行计划生成成功");
            System.out.println("🎯 计划类型: " + planResult.getPlan().getOperatorType());
            displayExecutionPlan(planResult.getPlan());
            System.out.println();
            
            return planResult.getPlan();
            
        } catch (SQLCompilerException e) {
            throw e; // 重新抛出编译异常
        } catch (Exception e) {
            System.out.println("❌ 编译过程异常: " + e.getMessage());
            throw new SQLCompilerException("SQL编译失败：" + e.getMessage(), sql, e);
        }
    }
    
    /**
     * 编译SELECT语句
     * @param sql SELECT语句
     * @return SelectPlan对象
     */
    public SelectPlan compileSelect(String sql) throws SQLCompilerException {
        LogicalPlan plan = compile(sql);
        if (!(plan instanceof SelectPlan)) {
            throw new SQLCompilerException("期望SELECT语句，但得到：" + plan.getClass().getSimpleName(), sql);
        }
        return (SelectPlan) plan;
    }
    
    /**
     * 编译INSERT语句
     * @param sql INSERT语句
     * @return InsertPlan对象
     */
    public InsertPlan compileInsert(String sql) throws SQLCompilerException {
        LogicalPlan plan = compile(sql);
        if (!(plan instanceof InsertPlan)) {
            throw new SQLCompilerException("期望INSERT语句，但得到：" + plan.getClass().getSimpleName(), sql);
        }
        return (InsertPlan) plan;
    }
    
    /**
     * 编译DELETE语句
     * @param sql DELETE语句
     * @return DeletePlan对象
     */
    public DeletePlan compileDelete(String sql) throws SQLCompilerException {
        LogicalPlan plan = compile(sql);
        if (!(plan instanceof DeletePlan)) {
            throw new SQLCompilerException("期望DELETE语句，但得到：" + plan.getClass().getSimpleName(), sql);
        }
        return (DeletePlan) plan;
    }
    
    /**
     * 编译CREATE TABLE语句
     * @param sql CREATE TABLE语句
     * @return CreateTablePlan对象
     */
    public CreateTablePlan compileCreateTable(String sql) throws SQLCompilerException {
        LogicalPlan plan = compile(sql);
        if (!(plan instanceof CreateTablePlan)) {
            throw new SQLCompilerException("期望CREATE TABLE语句，但得到：" + plan.getClass().getSimpleName(), sql);
        }
        return (CreateTablePlan) plan;
    }
    
    /**
     * 验证SQL语句语法和语义正确性（不生成执行计划）
     * @param sql SQL语句
     * @return 验证结果
     */
    public ValidationResult validate(String sql) {
        try {
            // 1. 词法分析
            SQLLexer lexer = new SQLLexer(sql);
            
            // 2. 语法分析
            SQLParser parser = new SQLParser(lexer.getAllTokens());
            ASTNode ast = parser.parse();
            
            if (ast == null) {
                return new ValidationResult(false, "语法分析失败");
            }
            
            // 3. 语义分析
            EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult analysisResult = semanticAnalyzer.analyze(ast);
            
            return new ValidationResult(analysisResult.getSemanticResult().isSuccess(), 
                analysisResult.getSemanticResult().isSuccess() ? "验证通过" : 
                (analysisResult.getSemanticResult().getErrors() != null ? 
                    analysisResult.getSemanticResult().getErrors().toString() : "语义分析失败"));
                
        } catch (Exception e) {
            return new ValidationResult(false, "验证失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取SQL语句的抽象语法树（用于调试）
     * @param sql SQL语句
     * @return AST节点
     */
    public ASTNode getAST(String sql) throws SQLCompilerException {
        try {
            SQLLexer lexer = new SQLLexer(sql);
            SQLParser parser = new SQLParser(lexer.getAllTokens());
            return parser.parse();
        } catch (Exception e) {
            throw new SQLCompilerException("AST生成失败：" + e.getMessage(), sql, e);
        }
    }
    
    /**
     * 获取语义分析器（用于高级功能）
     * @return 语义分析器实例
     */
    public EnhancedSemanticAnalyzer getSemanticAnalyzer() {
        return semanticAnalyzer;
    }
    
    /**
     * 显示AST结构
     */
    private void displayASTStructure(ASTNode node) {
        String nodeInfo = node.getClass().getSimpleName();
        
        // 添加节点特定信息
        if (nodeInfo.equals("SelectNode")) {
            List<String> columns = ASTFieldAccessor.getSelectColumns(node);
            String tableName = ASTFieldAccessor.getSelectTableName(node);
            nodeInfo += " (表:" + tableName + ", 列:" + columns + ")";
            
            // 检查ORDER BY和LIMIT
            Object orderBy = ASTFieldAccessor.getSelectOrderBy(node);
            Integer limit = ASTFieldAccessor.getSelectLimit(node);
            if (orderBy != null) nodeInfo += " ORDER BY:" + orderBy;
            if (limit != null) nodeInfo += " LIMIT:" + limit;
            
        } else if (nodeInfo.equals("CreateTableNode")) {
            String tableName = ASTFieldAccessor.getCreateTableName(node);
            List<Object> columns = ASTFieldAccessor.getCreateTableColumns(node);
            nodeInfo += " (表:" + tableName + ", 列数:" + columns.size() + ")";
            
        } else if (nodeInfo.equals("InsertNode")) {
            String tableName = ASTFieldAccessor.getInsertTableName(node);
            List<Object> values = ASTFieldAccessor.getInsertValues(node);
            nodeInfo += " (表:" + tableName + ", 值数:" + values.size() + ")";
            
        } else if (nodeInfo.equals("UpdateNode")) {
            String tableName = ASTFieldAccessor.getUpdateTableName(node);
            Map<String, Object> setValues = ASTFieldAccessor.getUpdateSetValues(node);
            nodeInfo += " (表:" + tableName + ", 更新列数:" + setValues.size() + ")";
            
        } else if (nodeInfo.equals("DeleteNode")) {
            String tableName = ASTFieldAccessor.getDeleteTableName(node);
            nodeInfo += " (表:" + tableName + ")";
        }
        
        System.out.println("  🌿 " + nodeInfo);
    }
    
    /**
     * 显示执行计划详细信息
     */
    private void displayExecutionPlan(LogicalPlan plan) {
        System.out.println("🌳 执行计划树形结构:");
        
        switch (plan.getOperatorType()) {
            case CREATE_TABLE:
                CreateTablePlan createPlan = (CreateTablePlan) plan;
                System.out.println("  📋 CreateTable");
                System.out.println("    ├─ 表名: " + createPlan.getTableName());
                System.out.println("    └─ 列数: " + createPlan.getColumns().size());
                break;
                
            case SELECT:
                SelectPlan selectPlan = (SelectPlan) plan;
                System.out.println("  📊 Project (投影)");
                System.out.println("    ├─ 输出列: " + selectPlan.getColumns());
                if (selectPlan.getFilter() != null) {
                    System.out.println("    ├─ 📍 Filter (过滤)");
                    System.out.println("    │   └─ 条件: " + selectPlan.getFilter());
                }
                System.out.println("    └─ 📖 SeqScan (顺序扫描)");
                System.out.println("        └─ 表: " + selectPlan.getTableName());
                break;
                
            case INSERT:
                InsertPlan insertPlan = (InsertPlan) plan;
                System.out.println("  📝 Insert");
                System.out.println("    ├─ 目标表: " + insertPlan.getTableName());
                System.out.println("    └─ 插入值: " + insertPlan.getValues());
                break;
                
            case UPDATE:
                UpdatePlan updatePlan = (UpdatePlan) plan;
                System.out.println("  ✏️ Update");
                System.out.println("    ├─ 目标表: " + updatePlan.getTableName());
                System.out.println("    ├─ 更新值: " + updatePlan.getSetValues());
                if (updatePlan.getFilter() != null) {
                    System.out.println("    └─ 更新条件: " + updatePlan.getFilter());
                } else {
                    System.out.println("    └─ 更新条件: 无 (全表更新)");
                }
                break;
                
            case DELETE:
                DeletePlan deletePlan = (DeletePlan) plan;
                System.out.println("  🗑️ Delete");
                System.out.println("    ├─ 目标表: " + deletePlan.getTableName());
                if (deletePlan.getFilter() != null) {
                    System.out.println("    └─ 删除条件: " + deletePlan.getFilter());
                } else {
                    System.out.println("    └─ 删除条件: 无 (全表删除)");
                }
                break;
                
            case CREATE_INDEX:
            case DROP_INDEX:
                System.out.println("  🔧 " + plan.getOperatorType() + " (索引操作)");
                break;
                
            default:
                System.out.println("  🔧 " + plan.getOperatorType() + " (详细信息暂不支持)");
                break;
        }
        
        System.out.println("\n💾 JSON格式执行计划:");
        System.out.println(formatPlanAsJSON(plan));
    }
    
    /**
     * 将执行计划格式化为JSON
     */
    private String formatPlanAsJSON(LogicalPlan plan) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"operator\": \"").append(plan.getOperatorType()).append("\",\n");
        
        switch (plan.getOperatorType()) {
            case CREATE_TABLE:
                CreateTablePlan createPlan = (CreateTablePlan) plan;
                json.append("  \"tableName\": \"").append(createPlan.getTableName()).append("\",\n");
                json.append("  \"columnCount\": ").append(createPlan.getColumns().size()).append("\n");
                break;
                
            case SELECT:
                SelectPlan selectPlan = (SelectPlan) plan;
                json.append("  \"tableName\": \"").append(selectPlan.getTableName()).append("\",\n");
                json.append("  \"columns\": ").append(selectPlan.getColumns()).append(",\n");
                if (selectPlan.getFilter() != null) {
                    json.append("  \"filter\": \"").append(selectPlan.getFilter()).append("\",\n");
                }
                json.append("  \"estimatedRows\": \"unknown\"\n");
                break;
                
            case INSERT:
                InsertPlan insertPlan = (InsertPlan) plan;
                json.append("  \"tableName\": \"").append(insertPlan.getTableName()).append("\",\n");
                json.append("  \"values\": ").append(insertPlan.getValues()).append("\n");
                break;
                
            case UPDATE:
                UpdatePlan updatePlan = (UpdatePlan) plan;
                json.append("  \"tableName\": \"").append(updatePlan.getTableName()).append("\",\n");
                json.append("  \"setValues\": ").append(updatePlan.getSetValues()).append(",\n");
                if (updatePlan.getFilter() != null) {
                    json.append("  \"filter\": \"").append(updatePlan.getFilter()).append("\",\n");
                }
                json.append("  \"estimatedAffectedRows\": \"unknown\"\n");
                break;
                
            case DELETE:
                DeletePlan deletePlan = (DeletePlan) plan;
                json.append("  \"tableName\": \"").append(deletePlan.getTableName()).append("\",\n");
                if (deletePlan.getFilter() != null) {
                    json.append("  \"filter\": \"").append(deletePlan.getFilter()).append("\",\n");
                }
                json.append("  \"estimatedAffectedRows\": \"unknown\"\n");
                break;
                
            case CREATE_INDEX:
            case DROP_INDEX:
                json.append("  \"operationType\": \"").append(plan.getOperatorType()).append("\"\n");
                break;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 重复字符串
     */
    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
