package semantic;

import parser.SQLLexer;
import parser.SQLParser;
import parser.ASTNode;
import common.plan.*;

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
     * @param sql SQL语句
     * @return LogicalPlan子类对象，如果编译失败则返回null
     * @throws SQLCompilerException 编译过程中的异常
     */
    public LogicalPlan compile(String sql) throws SQLCompilerException {
        try {
            // 1. 词法分析
            SQLLexer lexer = new SQLLexer(sql);
            // 注意：Token类在parser包中不是public的，我们通过getAllTokens()方法间接使用
            
            // 2. 语法分析
            SQLParser parser = new SQLParser(lexer.getAllTokens());
            ASTNode ast = parser.parse();
            
            if (ast == null) {
                throw new SQLCompilerException("语法分析失败：无法解析SQL语句", sql);
            }
            
            // 3. 语义分析
            EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult analysisResult = semanticAnalyzer.analyze(ast);
            if (!analysisResult.isFullySuccessful()) {
                String errorMsg = analysisResult.getSemanticResult().getErrors() != null ? 
                    analysisResult.getSemanticResult().getErrors().toString() : "语义分析失败";
                throw new SQLCompilerException("语义分析失败：" + errorMsg, sql);
            }
            
            // 4. 生成执行计划
            PlanGenerationResult planResult = semanticAnalyzer.generatePlan(ast);
            if (!planResult.isSuccess()) {
                throw new SQLCompilerException("执行计划生成失败：" + planResult.getErrors(), sql);
            }
            
            return planResult.getPlan();
            
        } catch (Exception e) {
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
}
