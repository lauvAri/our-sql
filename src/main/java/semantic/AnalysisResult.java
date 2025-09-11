package semantic;

import java.util.List;

/**
 * 语义分析结果
 * 包含生成的四元式、错误信息和分析状态
 */
public class AnalysisResult {
    private List<Quadruple> quadruples;          // 生成的四元式序列
    private List<SemanticError> errors;          // 错误信息列表（新格式）
    private List<String> legacyErrors;           // 旧格式错误（向后兼容）
    private boolean success;                     // 分析是否成功
    
    public AnalysisResult(List<Quadruple> quadruples, List<String> legacyErrors, boolean success) {
        this.quadruples = quadruples;
        this.legacyErrors = legacyErrors;
        this.errors = null;
        this.success = success;
    }
    
    public AnalysisResult(List<Quadruple> quadruples, List<SemanticError> errors, boolean success, boolean isNewFormat) {
        this.quadruples = quadruples;
        this.errors = errors;
        this.legacyErrors = null;
        this.success = success;
    }
    
    // Getter方法
    public List<Quadruple> getQuadruples() { return quadruples; }
    public List<String> getLegacyErrors() { return legacyErrors; }
    public List<SemanticError> getErrors() { return errors; }
    public boolean isSuccess() { return success; }
    
    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return (errors != null && !errors.isEmpty()) || 
               (legacyErrors != null && !legacyErrors.isEmpty());
    }
    
    /**
     * 获取错误数量
     */
    public int getErrorCount() {
        if (errors != null) {
            return errors.size();
        } else if (legacyErrors != null) {
            return legacyErrors.size();
        }
        return 0;
    }
    
    /**
     * 获取四元式数量
     */
    public int getQuadrupleCount() {
        return quadruples != null ? quadruples.size() : 0;
    }
    
    /**
     * 获取格式化的分析结果
     */
    public String getFormattedResult() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== 语义分析结果 ===\n");
        sb.append("分析状态: ").append(success ? "成功" : "失败").append("\n");
        
        if (hasErrors()) {
            sb.append("\n错误信息:\n");
            if (errors != null) {
                // 新格式错误
                for (int i = 0; i < errors.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(errors.get(i).toString()).append("\n");
                }
            } else if (legacyErrors != null) {
                // 旧格式错误（向后兼容）
                for (int i = 0; i < legacyErrors.size(); i++) {
                    sb.append("  ").append(i + 1).append(". ").append(legacyErrors.get(i)).append("\n");
                }
            }
        }
        
        if (quadruples != null && !quadruples.isEmpty()) {
            sb.append("\n生成的四元式:\n");
            for (int i = 0; i < quadruples.size(); i++) {
                sb.append("  ").append(i + 1).append(": ").append(quadruples.get(i).toString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("AnalysisResult{success=%s, errors=%d, quadruples=%d}", 
                           success, getErrorCount(), getQuadrupleCount());
    }
}
