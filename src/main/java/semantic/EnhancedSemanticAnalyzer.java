package semantic;

import parser.*;
import common.plan.LogicalPlan;
import java.util.*;

/**
 * 增强的语义分析器
 * 结合语义分析和执行计划生成
 */
public class EnhancedSemanticAnalyzer {
    private Catalog catalog;
    private SemanticAnalyzer semanticAnalyzer;
    private PlanGenerator planGenerator;
    
    public EnhancedSemanticAnalyzer(Catalog catalog) {
        this.catalog = catalog;
        this.semanticAnalyzer = new SemanticAnalyzer(catalog);
        this.planGenerator = new PlanGenerator(catalog);
    }
    
    /**
     * 完整分析：语义分析 + 执行计划生成
     */
    public ComprehensiveAnalysisResult analyze(ASTNode ast) {
        // 1. 语义分析
        AnalysisResult semanticResult = semanticAnalyzer.analyze(ast);
        
        // 2. 如果语义分析成功，生成执行计划
        PlanGenerationResult planResult = null;
        if (semanticResult.isSuccess()) {
            planResult = planGenerator.generatePlan(ast);
        }
        
        return new ComprehensiveAnalysisResult(semanticResult, planResult);
    }
    
    /**
     * 仅语义分析
     */
    public AnalysisResult analyzeSemantics(ASTNode ast) {
        return semanticAnalyzer.analyze(ast);
    }
    
    /**
     * 仅执行计划生成（假设语义已正确）
     */
    public PlanGenerationResult generatePlan(ASTNode ast) {
        return planGenerator.generatePlan(ast);
    }
    
    /**
     * 获取数据库目录
     */
    public Catalog getCatalog() {
        return catalog;
    }
    
    /**
     * 综合分析结果
     */
    public static class ComprehensiveAnalysisResult {
        private AnalysisResult semanticResult;
        private PlanGenerationResult planResult;
        
        public ComprehensiveAnalysisResult(AnalysisResult semanticResult, 
                                         PlanGenerationResult planResult) {
            this.semanticResult = semanticResult;
            this.planResult = planResult;
        }
        
        public AnalysisResult getSemanticResult() { return semanticResult; }
        public PlanGenerationResult getPlanResult() { return planResult; }
        
        public boolean isFullySuccessful() {
            return semanticResult.isSuccess() && 
                   (planResult == null || planResult.isSuccess());
        }
        
        public String getFormattedResult() {
            StringBuilder sb = new StringBuilder();
            
            sb.append("=== 综合语义分析结果 ===\n");
            sb.append("整体状态: ").append(isFullySuccessful() ? "成功" : "失败").append("\n\n");
            
            // 语义分析结果
            sb.append(semanticResult.getFormattedResult());
            
            // 执行计划生成结果
            if (planResult != null) {
                sb.append("\n").append(planResult.getFormattedResult());
            } else {
                sb.append("\n=== 执行计划生成结果 ===\n");
                sb.append("由于语义分析失败，跳过执行计划生成\n");
            }
            
            return sb.toString();
        }
        
        /**
         * 获取生成的执行计划（如果成功）
         */
        public LogicalPlan getExecutionPlan() {
            return planResult != null ? planResult.getPlan() : null;
        }
        
        /**
         * 获取所有错误信息
         */
        public List<String> getAllErrors() {
            List<String> allErrors = new ArrayList<>();
            
            // 添加语义分析错误
            if (semanticResult.getErrors() != null) {
                for (SemanticError error : semanticResult.getErrors()) {
                    allErrors.add(error.toString());
                }
            } else if (semanticResult.getLegacyErrors() != null) {
                allErrors.addAll(semanticResult.getLegacyErrors());
            }
            
            // 添加执行计划生成错误
            if (planResult != null && planResult.hasErrors()) {
                for (SemanticError error : planResult.getErrors()) {
                    allErrors.add(error.toString());
                }
            }
            
            return allErrors;
        }
    }
}
