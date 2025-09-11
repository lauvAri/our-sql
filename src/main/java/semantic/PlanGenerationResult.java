package semantic;

import common.plan.LogicalPlan;
import java.util.List;

/**
 * 执行计划生成结果
 */
public class PlanGenerationResult {
    private LogicalPlan plan;              // 生成的执行计划
    private List<SemanticError> errors;    // 错误信息列表
    private boolean success;               // 生成是否成功
    
    public PlanGenerationResult(LogicalPlan plan, List<SemanticError> errors, boolean success) {
        this.plan = plan;
        this.errors = errors;
        this.success = success;
    }
    
    // Getter方法
    public LogicalPlan getPlan() { return plan; }
    public List<SemanticError> getErrors() { return errors; }
    public boolean isSuccess() { return success; }
    
    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * 获取错误数量
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }
    
    /**
     * 获取格式化的结果
     */
    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 执行计划生成结果 ===\n");
        sb.append("生成状态: ").append(success ? "成功" : "失败").append("\n");
        
        if (hasErrors()) {
            sb.append("\n错误信息:\n");
            for (int i = 0; i < errors.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(errors.get(i).toString()).append("\n");
            }
        }
        
        if (plan != null) {
            sb.append("\n生成的执行计划:\n");
            sb.append(formatPlan(plan, 0));
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化执行计划
     */
    private String formatPlan(LogicalPlan plan, int indent) {
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);
        
        sb.append(indentStr).append("- ").append(plan.getOperatorType()).append("\n");
        
        // 根据计划类型添加详细信息
        switch (plan.getOperatorType()) {
            case SELECT:
                if (plan instanceof common.plan.SelectPlan) {
                    common.plan.SelectPlan selectPlan = (common.plan.SelectPlan) plan;
                    sb.append(indentStr).append("  表: ").append(selectPlan.getTableName()).append("\n");
                    sb.append(indentStr).append("  列: ").append(selectPlan.getColumns()).append("\n");
                    if (selectPlan.getFilter() != null) {
                        sb.append(indentStr).append("  过滤条件: ").append(selectPlan.getFilter()).append("\n");
                    }
                }
                break;
            case CREATE_TABLE:
                if (plan instanceof common.plan.CreateTablePlan) {
                    common.plan.CreateTablePlan createPlan = (common.plan.CreateTablePlan) plan;
                    sb.append(indentStr).append("  表: ").append(createPlan.getTableName()).append("\n");
                    sb.append(indentStr).append("  列: ").append(createPlan.getColumns()).append("\n");
                }
                break;
            case INSERT:
                if (plan instanceof common.plan.InsertPlan) {
                    common.plan.InsertPlan insertPlan = (common.plan.InsertPlan) plan;
                    sb.append(indentStr).append("  表: ").append(insertPlan.getTableName()).append("\n");
                    sb.append(indentStr).append("  值: ").append(insertPlan.getValues()).append("\n");
                }
                break;
            case DELETE:
                if (plan instanceof common.plan.DeletePlan) {
                    common.plan.DeletePlan deletePlan = (common.plan.DeletePlan) plan;
                    sb.append(indentStr).append("  表: ").append(deletePlan.getTableName()).append("\n");
                    if (deletePlan.getFilter() != null) {
                        sb.append(indentStr).append("  过滤条件: ").append(deletePlan.getFilter()).append("\n");
                    }
                }
                break;
            case CREATE_INDEX:
                if (plan instanceof common.plan.CreateIndexPlan) {
                    common.plan.CreateIndexPlan indexPlan = (common.plan.CreateIndexPlan) plan;
                    sb.append(indentStr).append("  索引: ").append(indexPlan.getIndexName()).append("\n");
                    sb.append(indentStr).append("  表: ").append(indexPlan.getTableName()).append("\n");
                    sb.append(indentStr).append("  列: ").append(indexPlan.getColumns()).append("\n");
                }
                break;
            case DROP_INDEX:
                if (plan instanceof common.plan.DropIndexPlan) {
                    common.plan.DropIndexPlan dropPlan = (common.plan.DropIndexPlan) plan;
                    sb.append(indentStr).append("  索引: ").append(dropPlan.getIndexName()).append("\n");
                    sb.append(indentStr).append("  表: ").append(dropPlan.getTableName()).append("\n");
                }
                break;
        }
        
        return sb.toString();
    }
    
    /**
     * 获取JSON格式的执行计划
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"success\": ").append(success).append(",\n");
        sb.append("  \"errorCount\": ").append(getErrorCount()).append(",\n");
        
        if (hasErrors()) {
            sb.append("  \"errors\": [\n");
            for (int i = 0; i < errors.size(); i++) {
                SemanticError error = errors.get(i);
                sb.append("    {\n");
                sb.append("      \"type\": \"").append(error.getErrorType()).append("\",\n");
                sb.append("      \"position\": \"").append(error.getPosition()).append("\",\n");
                sb.append("      \"description\": \"").append(error.getDescription()).append("\"\n");
                sb.append("    }");
                if (i < errors.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");
        }
        
        if (plan != null) {
            sb.append("  \"plan\": {\n");
            sb.append("    \"type\": \"").append(plan.getOperatorType()).append("\"\n");
            sb.append("  }\n");
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("PlanGenerationResult{success=%s, errors=%d, plan=%s}", 
                           success, getErrorCount(), plan != null ? plan.getOperatorType() : "null");
    }
}
