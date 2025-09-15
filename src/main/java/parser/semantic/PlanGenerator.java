package parser.semantic;

import parser.*;
import common.plan.*;
import common.Column;
import executor.expression.*;
import java.util.*;

/**
 * 执行计划生成器
 * 将AST转换为逻辑执行计划
 * 使用CatalogInterface与executor模块集成
 */
public class PlanGenerator {
    private CatalogInterface catalog;
    private List<SemanticError> errors;
    
    public PlanGenerator(CatalogInterface catalog) {
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
                case "CREATE_INDEX":
                    plan = generateCreateIndexPlan(ast);
                    break;
                case "DROP_INDEX":
                    plan = generateDropIndexPlan(ast);
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
        
        TableMetadata table = catalog.getTable(tableName);
        
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
        
        TableMetadata table = catalog.getTable(tableName);
        
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
                ColumnMetadata columnMetadata = table.getColumn(columnName);
                String expectedType = columnMetadata.getDataType();
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
        
        TableMetadata table = catalog.getTable(tableName);
        
        // 处理WHERE子句
        Expression filter = null;
        if (whereClause != null) {
            filter = buildExpression(whereClause, table);
        }
        
        return new DeletePlan(tableName, filter);
    }
    
    /**
     * 生成CREATE INDEX执行计划
     */
    private LogicalPlan generateCreateIndexPlan(ASTNode ast) {
        // 注意：这里需要根据实际的AST结构来获取索引相关信息
        // 由于当前语法分析器可能还不支持索引语法，这里提供一个框架实现
        
        // 假设索引AST包含索引名、表名、列名等信息
        // 实际实现需要根据AST的具体结构来调整
        
        addError(SemanticError.ErrorType.SYNTAX_ERROR, "CREATE INDEX", 
               "CREATE INDEX语句暂未完全实现，需要扩展语法分析器支持");
        return null;
        
        // 未来的实现示例：
        /*
        String indexName = ASTFieldAccessor.getCreateIndexName(ast);
        String tableName = ASTFieldAccessor.getCreateIndexTableName(ast);
        List<String> columns = ASTFieldAccessor.getCreateIndexColumns(ast);
        boolean isUnique = ASTFieldAccessor.getCreateIndexUnique(ast);
        
        // 检查表是否存在
        if (!catalog.tableExists(tableName)) {
            addError(SemanticError.ErrorType.TABLE_NOT_FOUND, "CREATE INDEX", 
                   "表 '" + tableName + "' 不存在");
            return null;
        }
        
        TableMetadata table = catalog.getTable(tableName);
        
        // 检查列是否存在
        for (String column : columns) {
            if (!table.hasColumn(column)) {
                addError(SemanticError.ErrorType.COLUMN_NOT_FOUND, "CREATE INDEX", 
                       "列 '" + column + "' 在表 '" + tableName + "' 中不存在");
            }
        }
        
        return new CreateIndexPlan(indexName, tableName, columns, isUnique);
        */
    }
    
    /**
     * 生成DROP INDEX执行计划
     */
    private LogicalPlan generateDropIndexPlan(ASTNode ast) {
        // 注意：这里需要根据实际的AST结构来获取索引相关信息
        // 由于当前语法分析器可能还不支持索引语法，这里提供一个框架实现
        
        addError(SemanticError.ErrorType.SYNTAX_ERROR, "DROP INDEX", 
               "DROP INDEX语句暂未完全实现，需要扩展语法分析器支持");
        return null;
        
        // 未来的实现示例：
        /*
        String indexName = ASTFieldAccessor.getDropIndexName(ast);
        String tableName = ASTFieldAccessor.getDropIndexTableName(ast);
        
        return new DropIndexPlan(indexName, tableName);
        */
    }
    
    /**
     * 构建表达式
     */
    private Expression buildExpression(Object expr, TableMetadata table) {
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
    private Expression buildOperand(Object operand, TableMetadata table) {
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
    
    /**
     * 验证LogicalPlan对象类型是否正确
     */
    public static String validatePlanType(LogicalPlan plan) {
        if (plan == null) {
            return "Plan is null";
        }
        
        StringBuilder validation = new StringBuilder();
        validation.append("Plan type: ").append(plan.getClass().getSimpleName()).append("\n");
        validation.append("Operator type: ").append(plan.getOperatorType()).append("\n");
        
        // 验证对象类型和操作类型的一致性
        switch (plan.getOperatorType()) {
            case SELECT:
                if (!(plan instanceof SelectPlan)) {
                    validation.append("❌ 错误: SELECT操作应该创建SelectPlan对象\n");
                } else {
                    validation.append("✅ 正确: SELECT操作创建了SelectPlan对象\n");
                    SelectPlan selectPlan = (SelectPlan) plan;
                    validation.append("  - 表: ").append(selectPlan.getTableName()).append("\n");
                    validation.append("  - 列: ").append(selectPlan.getColumns()).append("\n");
                    validation.append("  - 过滤条件: ").append(selectPlan.getFilter()).append("\n");
                }
                break;
                
            case CREATE_TABLE:
                if (!(plan instanceof CreateTablePlan)) {
                    validation.append("❌ 错误: CREATE_TABLE操作应该创建CreateTablePlan对象\n");
                } else {
                    validation.append("✅ 正确: CREATE_TABLE操作创建了CreateTablePlan对象\n");
                    CreateTablePlan createPlan = (CreateTablePlan) plan;
                    validation.append("  - 表: ").append(createPlan.getTableName()).append("\n");
                    validation.append("  - 列: ").append(createPlan.getColumns()).append("\n");
                }
                break;
                
            case INSERT:
                if (!(plan instanceof InsertPlan)) {
                    validation.append("❌ 错误: INSERT操作应该创建InsertPlan对象\n");
                } else {
                    validation.append("✅ 正确: INSERT操作创建了InsertPlan对象\n");
                    InsertPlan insertPlan = (InsertPlan) plan;
                    validation.append("  - 表: ").append(insertPlan.getTableName()).append("\n");
                    validation.append("  - 值: ").append(insertPlan.getValues()).append("\n");
                }
                break;
                
            case DELETE:
                if (!(plan instanceof DeletePlan)) {
                    validation.append("❌ 错误: DELETE操作应该创建DeletePlan对象\n");
                } else {
                    validation.append("✅ 正确: DELETE操作创建了DeletePlan对象\n");
                    DeletePlan deletePlan = (DeletePlan) plan;
                    validation.append("  - 表: ").append(deletePlan.getTableName()).append("\n");
                    validation.append("  - 过滤条件: ").append(deletePlan.getFilter()).append("\n");
                }
                break;
                
            case CREATE_INDEX:
                if (!(plan instanceof CreateIndexPlan)) {
                    validation.append("❌ 错误: CREATE_INDEX操作应该创建CreateIndexPlan对象\n");
                } else {
                    validation.append("✅ 正确: CREATE_INDEX操作创建了CreateIndexPlan对象\n");
                    CreateIndexPlan indexPlan = (CreateIndexPlan) plan;
                    validation.append("  - 索引: ").append(indexPlan.getIndexName()).append("\n");
                    validation.append("  - 表: ").append(indexPlan.getTableName()).append("\n");
                    validation.append("  - 列: ").append(indexPlan.getColumns()).append("\n");
                    validation.append("  - 唯一: ").append(indexPlan.isUnique()).append("\n");
                }
                break;
                
            case DROP_INDEX:
                if (!(plan instanceof DropIndexPlan)) {
                    validation.append("❌ 错误: DROP_INDEX操作应该创建DropIndexPlan对象\n");
                } else {
                    validation.append("✅ 正确: DROP_INDEX操作创建了DropIndexPlan对象\n");
                    DropIndexPlan dropPlan = (DropIndexPlan) plan;
                    validation.append("  - 索引: ").append(dropPlan.getIndexName()).append("\n");
                    validation.append("  - 表: ").append(dropPlan.getTableName()).append("\n");
                }
                break;
                
            default:
                validation.append("❓ 未知的操作类型: ").append(plan.getOperatorType()).append("\n");
        }
        
        return validation.toString();
    }
}
