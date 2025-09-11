package semantic;

import parser.*;
import common.plan.*;
import common.Column;
import executor.expression.*;
import java.util.*;

/**
 * 执行计划生成器
 * 将AST转换为逻辑执行计划
 */
public class PlanGenerator {
    private Catalog catalog;
    private List<SemanticError> errors;
    
    public PlanGenerator(Catalog catalog) {
        this.catalog = catalog;
        this.errors = new ArrayList<>();
    }
    
    /**
     * 生成执行计划
     */
    public PlanGenerationResult generatePlan(ASTNode ast) {
        errors.clear();
        
        try {
            LogicalPlan plan = null;
            String nodeType = ast.getType();
            
            switch (nodeType) {
                case "SELECT":
                    plan = generateSelectPlan(ast);
                    break;
                case "CREATE_TABLE":
                    plan = generateCreateTablePlan(ast);
                    break;
                case "INSERT":
                    plan = generateInsertPlan(ast);
                    break;
                case "DELETE":
                    plan = generateDeletePlan(ast);
                    break;
                default:
                    addError(SemanticError.ErrorType.SYNTAX_ERROR, "UNKNOWN", 
                           "不支持的语句类型: " + nodeType);
            }
            
            return new PlanGenerationResult(plan, new ArrayList<>(errors), errors.isEmpty());
            
        } catch (Exception e) {
            addError(SemanticError.ErrorType.SYNTAX_ERROR, "GENERATOR", 
                   "执行计划生成失败: " + e.getMessage());
            return new PlanGenerationResult(null, new ArrayList<>(errors), false);
        }
    }
    
    /**
     * 生成SELECT执行计划
     */
    private LogicalPlan generateSelectPlan(ASTNode ast) {
        String tableName = ASTFieldAccessor.getSelectTableName(ast);
        List<String> columns = ASTFieldAccessor.getSelectColumns(ast);
        Object whereClause = ASTFieldAccessor.getSelectWhereClause(ast);
        
        // 检查表是否存在
        if (!catalog.tableExists(tableName)) {
            addError(SemanticError.ErrorType.TABLE_NOT_FOUND, "FROM", 
                   "表 '" + tableName + "' 不存在");
            return null;
        }
        
        TableSchema table = catalog.getTable(tableName);
        
        // 检查列是否存在
        for (String column : columns) {
            if (!column.equals("*") && !table.hasColumn(column)) {
                addError(SemanticError.ErrorType.COLUMN_NOT_FOUND, "SELECT", 
                       "列 '" + column + "' 在表 '" + tableName + "' 中不存在");
            }
        }
        
        // 处理WHERE子句
        Expression filter = null;
        if (whereClause != null) {
            filter = buildExpression(whereClause, table);
        }
        
        return new SelectPlan(tableName, columns, filter);
    }
    
    /**
     * 生成CREATE TABLE执行计划
     */
    private LogicalPlan generateCreateTablePlan(ASTNode ast) {
        String tableName = ASTFieldAccessor.getCreateTableName(ast);
        List<Object> columnDefs = ASTFieldAccessor.getCreateTableColumns(ast);
        
        // 检查表是否已存在
        if (catalog.tableExists(tableName)) {
            addError(SemanticError.ErrorType.TABLE_ALREADY_EXISTS, "CREATE TABLE", 
                   "表 '" + tableName + "' 已存在");
            return null;
        }
        
        List<Column> columns = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();
        
        for (Object colDefObj : columnDefs) {
            String columnName = ASTFieldAccessor.getColumnDefinitionName(colDefObj);
            String dataType = ASTFieldAccessor.getColumnDefinitionDataType(colDefObj);
            
            // 检查重复列名
            if (columnNames.contains(columnName.toLowerCase())) {
                addError(SemanticError.ErrorType.DUPLICATE_COLUMN, "CREATE TABLE", 
                       "列名 '" + columnName + "' 重复");
                continue;
            }
            columnNames.add(columnName.toLowerCase());
            
            // 检查数据类型是否有效
            if (!isValidDataType(dataType)) {
                addError(SemanticError.ErrorType.INVALID_DATA_TYPE, "CREATE TABLE", 
                       "无效的数据类型: " + dataType);
                continue;
            }
            
            columns.add(new Column(columnName, dataType, 255)); // 默认长度255
        }
        
        return new CreateTablePlan(tableName, columns);
    }
    
    /**
     * 生成INSERT执行计划
     */
    private LogicalPlan generateInsertPlan(ASTNode ast) {
        String tableName = ASTFieldAccessor.getInsertTableName(ast);
        List<String> columns = ASTFieldAccessor.getInsertColumns(ast);
        List<Object> values = ASTFieldAccessor.getInsertValues(ast);
        
        // 检查表是否存在
        if (!catalog.tableExists(tableName)) {
            addError(SemanticError.ErrorType.TABLE_NOT_FOUND, "INSERT INTO", 
                   "表 '" + tableName + "' 不存在");
            return null;
        }
        
        TableSchema table = catalog.getTable(tableName);
        
        // 检查列是否存在
        for (String column : columns) {
            if (!table.hasColumn(column)) {
                addError(SemanticError.ErrorType.COLUMN_NOT_FOUND, "INSERT", 
                       "列 '" + column + "' 在表 '" + tableName + "' 中不存在");
            }
        }
        
        // 检查列数是否匹配
        if (columns.size() != values.size()) {
            addError(SemanticError.ErrorType.COLUMN_COUNT_MISMATCH, "INSERT", 
                   "列数不匹配，期望 " + columns.size() + "，实际 " + values.size());
            return null;
        }
        
        // 检查类型兼容性
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            Object value = values.get(i);
            
            if (table.hasColumn(columnName)) {
                ColumnSchema columnSchema = table.getColumn(columnName);
                String expectedType = columnSchema.getDataType();
                String actualType = getValueType(value);
                
                // 改进类型兼容性检查
                if (!isTypeCompatible(actualType, expectedType)) {
                    addError(SemanticError.ErrorType.TYPE_MISMATCH, "INSERT", 
                           "列 '" + columnName + "' 类型不匹配，期望 " + expectedType + 
                           "，实际 " + actualType);
                }
            }
        }
        
        // 构建插入值列表
        List<List<Object>> valuesList = new ArrayList<>();
        valuesList.add(values);
        
        return new InsertPlan(tableName, valuesList);
    }
    
    /**
     * 生成DELETE执行计划
     */
    private LogicalPlan generateDeletePlan(ASTNode ast) {
        String tableName = ASTFieldAccessor.getDeleteTableName(ast);
        Object whereClause = ASTFieldAccessor.getDeleteWhereClause(ast);
        
        // 检查表是否存在
        if (!catalog.tableExists(tableName)) {
            addError(SemanticError.ErrorType.TABLE_NOT_FOUND, "DELETE FROM", 
                   "表 '" + tableName + "' 不存在");
            return null;
        }
        
        TableSchema table = catalog.getTable(tableName);
        
        // 处理WHERE子句
        Expression filter = null;
        if (whereClause != null) {
            filter = buildExpression(whereClause, table);
        }
        
        return new DeletePlan(tableName, filter);
    }
    
    /**
     * 构建表达式
     */
    private Expression buildExpression(Object expr, TableSchema table) {
        if (expr == null) {
            return null;
        }
        
        Object left = ASTFieldAccessor.getExpressionLeft(expr);
        String operator = ASTFieldAccessor.getExpressionOperator(expr);
        Object right = ASTFieldAccessor.getExpressionRight(expr);
        
        Expression leftExpr = buildOperand(left, table);
        Expression rightExpr = buildOperand(right, table);
        
        if (leftExpr == null || rightExpr == null) {
            return null;
        }
        
        // 根据操作符类型创建表达式
        switch (operator) {
            case "=":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.EQ, rightExpr);
            case ">":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.GT, rightExpr);
            case "<":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.LT, rightExpr);
            case ">=":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.GTE, rightExpr);
            case "<=":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.LTE, rightExpr);
            case "<>":
            case "!=":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.NEQ, rightExpr);
            case "AND":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.AND, rightExpr);
            case "OR":
                return new BinaryExpression(leftExpr, BinaryExpression.Operator.OR, rightExpr);
            default:
                addError(SemanticError.ErrorType.SYNTAX_ERROR, "WHERE", 
                       "不支持的操作符: " + operator);
                return null;
        }
    }
    
    /**
     * 构建操作数表达式
     */
    private Expression buildOperand(Object operand, TableSchema table) {
        if (operand instanceof String) {
            String operandStr = (String) operand;
            
            // 检查是否为列名
            if (table.hasColumn(operandStr)) {
                return new ColumnReference(operandStr);
            } else {
                // 作为常量处理
                return new ConstantExpression(operandStr);
            }
        } else {
            // 其他类型作为常量处理
            return new ConstantExpression(operand);
        }
    }
    
    /**
     * 检查数据类型是否有效
     */
    private boolean isValidDataType(String dataType) {
        return dataType.equals("INT") || dataType.equals("VARCHAR") || 
               dataType.equals("FLOAT") || dataType.equals("DOUBLE") ||
               dataType.equals("BOOLEAN") || dataType.equals("DATE") ||
               dataType.equals("CHAR");
    }
    
    /**
     * 检查类型兼容性
     */
    private boolean isTypeCompatible(String actualType, String expectedType) {
        // 完全匹配
        if (actualType.equals(expectedType)) {
            return true;
        }
        
        // 数值类型兼容性
        if (isNumericType(actualType) && isNumericType(expectedType)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查是否为数值类型
     */
    private boolean isNumericType(String type) {
        return type.equals("INT") || type.equals("FLOAT") || type.equals("DOUBLE");
    }
    
    /**
     * 获取值的类型
     */
    private String getValueType(Object value) {
        if (value instanceof String) {
            String str = (String) value;
            // 尝试解析为数字
            try {
                Integer.parseInt(str);
                return "INT";
            } catch (NumberFormatException e1) {
                try {
                    Double.parseDouble(str);
                    return "FLOAT";
                } catch (NumberFormatException e2) {
                    // 检查是否为布尔值
                    if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
                        return "BOOLEAN";
                    }
                    return "VARCHAR";
                }
            }
        } else if (value instanceof Integer) {
            return "INT";
        } else if (value instanceof Float || value instanceof Double) {
            return "FLOAT";
        } else if (value instanceof Boolean) {
            return "BOOLEAN";
        }
        return "UNKNOWN";
    }
    
    /**
     * 添加错误
     */
    private void addError(SemanticError.ErrorType errorType, String position, String description) {
        errors.add(new SemanticError(errorType, position, description));
    }
    
    /**
     * 获取错误列表
     */
    public List<SemanticError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
