package parser.semantic;

import common.plan.*;
import java.util.*;

/**
 * 执行计划格式化器
 * 支持树形结构、JSON和S表达式格式
 */
public class PlanFormatter {
    
    public enum OutputFormat {
        TREE,       // 树形结构
        JSON,       // JSON格式
        S_EXPR      // S表达式
    }
    
    /**
     * 格式化执行计划
     */
    public static String format(LogicalPlan plan, OutputFormat format) {
        if (plan == null) {
            return "null";
        }
        
        switch (format) {
            case TREE:
                return formatAsTree(plan);
            case JSON:
                return formatAsJson(plan);
            case S_EXPR:
                return formatAsSExpression(plan);
            default:
                return plan.toString();
        }
    }
    
    /**
     * 格式化为树形结构
     */
    public static String formatAsTree(LogicalPlan plan) {
        return formatAsTree(plan, 0);
    }
    
    private static String formatAsTree(LogicalPlan plan, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(level);
        
        sb.append(indent).append("├─ ").append(plan.getOperatorType()).append("\n");
        
        switch (plan.getOperatorType()) {
            case SELECT:
                SelectPlan selectPlan = (SelectPlan) plan;
                sb.append(indent).append("│  ├─ SeqScan: ").append(selectPlan.getTableName()).append("\n");
                if (selectPlan.getFilter() != null) {
                    sb.append(indent).append("│  ├─ Filter: ").append(selectPlan.getFilter()).append("\n");
                }
                sb.append(indent).append("│  └─ Project: ").append(selectPlan.getColumns()).append("\n");
                break;
                
            case CREATE_TABLE:
                CreateTablePlan createPlan = (CreateTablePlan) plan;
                sb.append(indent).append("│  ├─ Table: ").append(createPlan.getTableName()).append("\n");
                sb.append(indent).append("│  └─ Columns: ").append(createPlan.getColumns()).append("\n");
                break;
                
            case INSERT:
                InsertPlan insertPlan = (InsertPlan) plan;
                sb.append(indent).append("│  ├─ Table: ").append(insertPlan.getTableName()).append("\n");
                sb.append(indent).append("│  └─ Values: ").append(insertPlan.getValues()).append("\n");
                break;
                
            case DELETE:
                DeletePlan deletePlan = (DeletePlan) plan;
                sb.append(indent).append("│  ├─ SeqScan: ").append(deletePlan.getTableName()).append("\n");
                if (deletePlan.getFilter() != null) {
                    sb.append(indent).append("│  └─ Filter: ").append(deletePlan.getFilter()).append("\n");
                }
                break;
                
            case CREATE_INDEX:
                CreateIndexPlan indexPlan = (CreateIndexPlan) plan;
                sb.append(indent).append("│  ├─ Index: ").append(indexPlan.getIndexName()).append("\n");
                sb.append(indent).append("│  ├─ Table: ").append(indexPlan.getTableName()).append("\n");
                sb.append(indent).append("│  └─ Columns: ").append(indexPlan.getColumns()).append("\n");
                break;
                
            case DROP_INDEX:
                DropIndexPlan dropPlan = (DropIndexPlan) plan;
                sb.append(indent).append("│  ├─ Index: ").append(dropPlan.getIndexName()).append("\n");
                sb.append(indent).append("│  └─ Table: ").append(dropPlan.getTableName()).append("\n");
                break;
        }
        
        return sb.toString();
    }
    
    /**
     * 格式化为JSON
     */
    public static String formatAsJson(LogicalPlan plan) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{\n");
        sb.append("  \"operator\": \"").append(plan.getOperatorType()).append("\",\n");
        
        switch (plan.getOperatorType()) {
            case SELECT:
                SelectPlan selectPlan = (SelectPlan) plan;
                sb.append("  \"children\": [\n");
                
                // SeqScan
                sb.append("    {\n");
                sb.append("      \"operator\": \"SeqScan\",\n");
                sb.append("      \"table\": \"").append(selectPlan.getTableName()).append("\"\n");
                sb.append("    }");
                
                // Filter
                if (selectPlan.getFilter() != null) {
                    sb.append(",\n    {\n");
                    sb.append("      \"operator\": \"Filter\",\n");
                    sb.append("      \"condition\": \"").append(selectPlan.getFilter()).append("\"\n");
                    sb.append("    }");
                }
                
                // Project
                sb.append(",\n    {\n");
                sb.append("      \"operator\": \"Project\",\n");
                sb.append("      \"columns\": ").append(formatColumnsAsJson(selectPlan.getColumns())).append("\n");
                sb.append("    }\n");
                
                sb.append("  ]\n");
                break;
                
            case CREATE_TABLE:
                CreateTablePlan createPlan = (CreateTablePlan) plan;
                sb.append("  \"table\": \"").append(createPlan.getTableName()).append("\",\n");
                sb.append("  \"columns\": ").append(formatColumnsAsJson(createPlan.getColumns())).append("\n");
                break;
                
            case INSERT:
                InsertPlan insertPlan = (InsertPlan) plan;
                sb.append("  \"table\": \"").append(insertPlan.getTableName()).append("\",\n");
                sb.append("  \"values\": ").append(formatValuesAsJson(insertPlan.getValues())).append("\n");
                break;
                
            case DELETE:
                DeletePlan deletePlan = (DeletePlan) plan;
                sb.append("  \"children\": [\n");
                sb.append("    {\n");
                sb.append("      \"operator\": \"SeqScan\",\n");
                sb.append("      \"table\": \"").append(deletePlan.getTableName()).append("\"\n");
                sb.append("    }");
                
                if (deletePlan.getFilter() != null) {
                    sb.append(",\n    {\n");
                    sb.append("      \"operator\": \"Filter\",\n");
                    sb.append("      \"condition\": \"").append(deletePlan.getFilter()).append("\"\n");
                    sb.append("    }");
                }
                
                sb.append("\n  ]\n");
                break;
                
            default:
                sb.append("  \"details\": \"").append(plan.toString()).append("\"\n");
                break;
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 格式化为S表达式
     */
    public static String formatAsSExpression(LogicalPlan plan) {
        switch (plan.getOperatorType()) {
            case SELECT:
                SelectPlan selectPlan = (SelectPlan) plan;
                StringBuilder sb = new StringBuilder();
                sb.append("(select ");
                sb.append("(project ").append(formatColumnsAsSExpr(selectPlan.getColumns())).append(") ");
                
                if (selectPlan.getFilter() != null) {
                    sb.append("(filter ").append(selectPlan.getFilter()).append(" ");
                    sb.append("(seq-scan ").append(selectPlan.getTableName()).append("))");
                } else {
                    sb.append("(seq-scan ").append(selectPlan.getTableName()).append(")");
                }
                
                sb.append(")");
                return sb.toString();
                
            case CREATE_TABLE:
                CreateTablePlan createPlan = (CreateTablePlan) plan;
                return String.format("(create-table %s %s)", 
                                   createPlan.getTableName(), 
                                   formatColumnsAsSExpr(createPlan.getColumns()));
                
            case INSERT:
                InsertPlan insertPlan = (InsertPlan) plan;
                return String.format("(insert %s %s)", 
                                   insertPlan.getTableName(), 
                                   formatValuesAsSExpr(insertPlan.getValues()));
                
            case DELETE:
                DeletePlan deletePlan = (DeletePlan) plan;
                if (deletePlan.getFilter() != null) {
                    return String.format("(delete (filter %s (seq-scan %s)))", 
                                       deletePlan.getFilter(), deletePlan.getTableName());
                } else {
                    return String.format("(delete (seq-scan %s))", deletePlan.getTableName());
                }
                
            default:
                return String.format("(%s %s)", plan.getOperatorType().toString().toLowerCase(), 
                                   plan.toString());
        }
    }
    
    // 辅助方法
    private static String formatColumnsAsJson(List<?> columns) {
        return "[" + String.join(", ", columns.stream()
                .map(c -> "\"" + c.toString() + "\"")
                .toArray(String[]::new)) + "]";
    }
    
    private static String formatValuesAsJson(List<List<Object>> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < values.size(); i++) {
            sb.append("[");
            for (int j = 0; j < values.get(i).size(); j++) {
                Object value = values.get(i).get(j);
                if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else {
                    sb.append(value);
                }
                if (j < values.get(i).size() - 1) sb.append(", ");
            }
            sb.append("]");
            if (i < values.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String formatColumnsAsSExpr(List<?> columns) {
        return "(" + String.join(" ", columns.stream()
                .map(Object::toString)
                .toArray(String[]::new)) + ")";
    }
    
    private static String formatValuesAsSExpr(List<List<Object>> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < values.size(); i++) {
            sb.append("(");
            for (int j = 0; j < values.get(i).size(); j++) {
                sb.append(values.get(i).get(j));
                if (j < values.get(i).size() - 1) sb.append(" ");
            }
            sb.append(")");
            if (i < values.size() - 1) sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }
}
