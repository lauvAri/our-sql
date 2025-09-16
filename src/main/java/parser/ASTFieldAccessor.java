package parser;

import java.lang.reflect.Field;
import java.util.List;

/**
 * AST节点访问器 - 提供对AST节点字段的安全访问
 */
public class ASTFieldAccessor {
    
    /**
     * 获取节点的字段值
     */
    public static Object getField(Object node, String fieldName) {
        try {
            Field field = node.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(node);
        } catch (Exception e) {
            System.err.println("获取字段 " + fieldName + " 失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取SelectNode的字段
     */
    public static String getSelectTableName(ASTNode node) {
        return (String) getField(node, "tableName");
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getSelectColumns(ASTNode node) {
        return (List<String>) getField(node, "columns");
    }
    
    public static Object getSelectWhereClause(ASTNode node) {
        return getField(node, "whereClause");
    }
    
    /**
     * 获取SelectNode的ORDER BY子句
     */
    public static Object getSelectOrderBy(ASTNode node) {
        return getField(node, "orderBy");
    }
    
    /**
     * 获取SelectNode的LIMIT值
     */
    public static Integer getSelectLimit(ASTNode node) {
        Object limit = getField(node, "limit");
        if (limit instanceof Integer) {
            return (Integer) limit;
        }
        return -1; // 表示无限制
    }
    
    /**
     * 获取CreateTableNode的字段
     */
    public static String getCreateTableName(ASTNode node) {
        return (String) getField(node, "tableName");
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> getCreateTableColumns(ASTNode node) {
        return (List<Object>) getField(node, "columns");
    }
    
    /**
     * 获取InsertNode的字段
     */
    public static String getInsertTableName(ASTNode node) {
        return (String) getField(node, "tableName");
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> getInsertColumns(ASTNode node) {
        return (List<String>) getField(node, "columns");
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> getInsertValues(ASTNode node) {
        return (List<Object>) getField(node, "values");
    }
    
    /**
     * 获取DeleteNode的字段
     */
    public static String getDeleteTableName(ASTNode node) {
        return (String) getField(node, "tableName");
    }
    
    public static Object getDeleteWhereClause(ASTNode node) {
        return getField(node, "whereClause");
    }
    
    /**
     * 获取ExpressionNode的字段
     */
    public static Object getExpressionLeft(Object expr) {
        return getField(expr, "left");
    }
    
    public static String getExpressionOperator(Object expr) {
        return (String) getField(expr, "operator");
    }
    
    public static Object getExpressionRight(Object expr) {
        return getField(expr, "right");
    }
    
    /**
     * 获取ColumnDefinition的字段
     */
    public static String getColumnDefinitionName(Object colDef) {
        return (String) getField(colDef, "name");
    }
    
    public static String getColumnDefinitionDataType(Object colDef) {
        return (String) getField(colDef, "dataType");
    }
}
