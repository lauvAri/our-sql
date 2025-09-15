package parser.semantic;

import parser.*;
import java.util.*;

/**
 * 语义分析器
 * 负责检查语义正确性并生成四元式中间代码
 * 使用CatalogInterface与executor模块的目录系统集成
 */
public class SemanticAnalyzer {
    private CatalogInterface catalog;  // 数据库目录接口
    private List<Quadruple> quadruples;  // 四元式序列
    private int tempVarCounter;  // 临时变量计数器
    private List<String> errors;  // 错误列表
    
    public SemanticAnalyzer(CatalogInterface catalog) {
        this.catalog = catalog;
        this.quadruples = new ArrayList<>();
        this.tempVarCounter = 1;
        this.errors = new ArrayList<>();
    }
    
    /**
     * 分析AST节点
     */
    public AnalysisResult analyze(ASTNode ast) {
        errors.clear();
        quadruples.clear();
        tempVarCounter = 1;
        
        try {
            String nodeType = ast.getType();
            switch (nodeType) {
                case "SELECT":
                    analyzeSelect(ast);
                    break;
                case "CREATE_TABLE":
                    analyzeCreateTable(ast);
                    break;
                case "INSERT":
                    analyzeInsert(ast);
                    break;
                case "DELETE":
                    analyzeDelete(ast);
                    break;
                default:
                    addError("未知的语句类型: " + nodeType);
            }
        } catch (Exception e) {
            addError("语义分析错误: " + e.getMessage());
        }
        
        return new AnalysisResult(
            new ArrayList<>(quadruples), 
            new ArrayList<>(errors),
            errors.isEmpty()
        );
    }
    
    /**
     * 分析SELECT语句
     */
    private void analyzeSelect(ASTNode ast) {
        try {
            String tableName = ASTFieldAccessor.getSelectTableName(ast);
            List<String> columns = ASTFieldAccessor.getSelectColumns(ast);
            Object whereClause = ASTFieldAccessor.getSelectWhereClause(ast);
            
            // 检查表是否存在
            if (!catalog.tableExists(tableName)) {
                addError("表 '" + tableName + "' 不存在");
                return;
            }
            
            TableMetadata table = catalog.getTable(tableName);
            
            // 检查选择的列
            List<String> selectedColumns = new ArrayList<>();
            boolean hasColumnErrors = false;
            if (columns.contains("*")) {
                // SELECT * - 选择所有列
                selectedColumns.addAll(table.getColumnNames());
            } else {
                // 检查每个指定的列是否存在
                for (String columnName : columns) {
                    if (!table.hasColumn(columnName)) {
                        addError("列 '" + columnName + "' 在表 '" + tableName + "' 中不存在");
                        hasColumnErrors = true;
                    } else {
                        selectedColumns.add(columnName);
                    }
                }
            }
            
            // 生成扫描表的四元式
            String tableTemp = newTempVar();
            addQuadruple("SCAN", tableName, null, tableTemp);
            
            // 分析WHERE条件（如果存在）
            String whereResult = tableTemp;
            if (whereClause != null) {
                whereResult = analyzeExpression(whereClause, table);
                if (whereResult != null) {
                    String filterTemp = newTempVar();
                    addQuadruple("FILTER", tableTemp, whereResult, filterTemp);
                    whereResult = filterTemp;
                }
            }
            
            // 生成投影四元式（只有在没有列错误时）
            if (!hasColumnErrors && !selectedColumns.isEmpty()) {
                String projectTemp = newTempVar();
                String columnList = String.join(",", selectedColumns);
                addQuadruple("PROJECT", whereResult, columnList, projectTemp);
            }
        } catch (Exception e) {
            addError("SELECT语句分析错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析CREATE TABLE语句
     */
    private void analyzeCreateTable(ASTNode ast) {
        try {
            String tableName = ASTFieldAccessor.getCreateTableName(ast);
            List<Object> columns = ASTFieldAccessor.getCreateTableColumns(ast);
            
            // 检查表是否已存在
            if (catalog.tableExists(tableName)) {
                addError("表 '" + tableName + "' 已存在");
                return;
            }
            
            // 检查列定义的有效性
            Set<String> columnNames = new HashSet<>();
            List<ColumnMetadata> columnMetadataList = new ArrayList<>();
            boolean hasDefinitionErrors = false;
            
            for (Object colDefObj : columns) {
                String name = ASTFieldAccessor.getColumnDefinitionName(colDefObj);
                String dataType = ASTFieldAccessor.getColumnDefinitionDataType(colDefObj);
                
                // 检查列名重复
                if (columnNames.contains(name)) {
                    addError("列名 '" + name + "' 重复");
                    hasDefinitionErrors = true;
                } else {
                    columnNames.add(name);
                }
                
                // 检查数据类型有效性
                if (!catalog.isValidDataType(dataType)) {
                    addError("无效的数据类型: " + dataType);
                    hasDefinitionErrors = true;
                }
                
                columnMetadataList.add(new ColumnMetadata(name, dataType, true));
            }
            
            // 如果没有错误，生成创建表的四元式
            if (!hasDefinitionErrors) {
                // 创建表结构
                StringBuilder columnsStr = new StringBuilder();
                for (int i = 0; i < columnMetadataList.size(); i++) {
                    ColumnMetadata col = columnMetadataList.get(i);
                    columnsStr.append(col.getColumnName()).append(":").append(col.getDataType());
                    if (i < columnMetadataList.size() - 1) {
                        columnsStr.append(",");
                    }
                }
                addQuadruple("CREATE_TABLE", tableName, columnsStr.toString(), null);
                
                // 注意：在适配器模式下，我们不直接添加到catalog
                // 这将由executor模块处理
            }
        } catch (Exception e) {
            addError("CREATE TABLE语句分析错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析INSERT语句
     */
    private void analyzeInsert(ASTNode ast) {
        try {
            String tableName = ASTFieldAccessor.getInsertTableName(ast);
            List<String> columns = ASTFieldAccessor.getInsertColumns(ast);
            List<Object> values = ASTFieldAccessor.getInsertValues(ast);
            
            // 检查表是否存在
            if (!catalog.tableExists(tableName)) {
                addError("表 '" + tableName + "' 不存在");
                return;
            }
            
            TableMetadata table = catalog.getTable(tableName);
            
            // 检查列数和值数是否匹配
            if (columns.size() != values.size()) {
                addError("列数(" + columns.size() + ")和值数(" + 
                        values.size() + ")不匹配");
                return;
            }
            
            // 检查每个列是否存在，并验证类型
            boolean hasColumnErrors = false;
            for (int i = 0; i < columns.size(); i++) {
                String columnName = columns.get(i);
                Object value = values.get(i);
                
                if (!table.hasColumn(columnName)) {
                    addError("列 '" + columnName + "' 在表 '" + tableName + "' 中不存在");
                    hasColumnErrors = true;
                    continue;
                }
                
                // 类型检查
                ColumnMetadata column = table.getColumn(columnName);
                if (!isTypeCompatible(value, column.getDataType())) {
                    addError("列 '" + columnName + "' 的值类型不匹配，期望 " + 
                            column.getDataType() + "，实际 " + getValueType(value));
                    hasColumnErrors = true;
                }
            }
            
            // 生成INSERT四元式
            if (!hasColumnErrors) {
                String columnList = String.join(",", columns);
                String valueList = values.stream()
                        .map(Object::toString)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
                addQuadruple("INSERT", tableName, columnList, valueList);
            }
        } catch (Exception e) {
            addError("INSERT语句分析错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析DELETE语句
     */
    private void analyzeDelete(ASTNode ast) {
        try {
            String tableName = ASTFieldAccessor.getDeleteTableName(ast);
            Object whereClause = ASTFieldAccessor.getDeleteWhereClause(ast);
            
            // 检查表是否存在
            if (!catalog.tableExists(tableName)) {
                addError("表 '" + tableName + "' 不存在");
                return;
            }
            
            TableMetadata table = catalog.getTable(tableName);
            
            // 分析WHERE条件
            String whereResult = null;
            if (whereClause != null) {
                whereResult = analyzeExpression(whereClause, table);
            }
            
            // 生成DELETE四元式
            addQuadruple("DELETE", tableName, whereResult, null);
        } catch (Exception e) {
            addError("DELETE语句分析错误: " + e.getMessage());
        }
    }
    
    /**
     * 分析表达式并生成四元式
     */
    private String analyzeExpression(Object expr, TableMetadata table) {
        if (expr == null) return null;
        
        try {
            Object left = ASTFieldAccessor.getExpressionLeft(expr);
            String operator = ASTFieldAccessor.getExpressionOperator(expr);
            Object right = ASTFieldAccessor.getExpressionRight(expr);
            
            String leftOperand = analyzeOperand(left, table);
            String rightOperand = analyzeOperand(right, table);
            
            if (leftOperand == null || rightOperand == null) {
                return null;
            }
            
            // 检查操作符两边的类型兼容性
            if (!areTypesCompatible(left, right, table)) {
                addError("操作符 '" + operator + "' 两边的类型不兼容");
                return null;
            }
            
            // 生成比较操作的四元式
            String resultTemp = newTempVar();
            addQuadruple(operator, leftOperand, rightOperand, resultTemp);
            
            return resultTemp;
        } catch (Exception e) {
            addError("表达式分析错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 分析操作数
     */
    private String analyzeOperand(Object operand, TableMetadata table) {
        if (operand instanceof String) {
            String str = (String) operand;
            // 检查是否为列名
            if (table.hasColumn(str)) {
                return str;  // 列名
            } else {
                // 可能是常量
                return str;
            }
        }
        return operand.toString();
    }
    
    /**
     * 检查类型兼容性
     */
    private boolean areTypesCompatible(Object left, Object right, TableMetadata table) {
        String leftType = getOperandType(left, table);
        String rightType = getOperandType(right, table);
        
        if (leftType == null || rightType == null) {
            return false;
        }
        
        // 简化的类型兼容性检查
        return leftType.equals(rightType) || 
               isNumericType(leftType) && isNumericType(rightType);
    }
    
    /**
     * 获取操作数的类型
     */
    private String getOperandType(Object operand, TableMetadata table) {
        if (operand instanceof String) {
            String str = (String) operand;
            if (table.hasColumn(str)) {
                return table.getColumn(str).getDataType();
            } else {
                // 尝试推断常量类型
                return inferConstantType(str);
            }
        }
        return null;
    }
    
    /**
     * 推断常量类型
     */
    private String inferConstantType(String value) {
        // 字符串常量
        if (value.startsWith("'") && value.endsWith("'")) {
            return "VARCHAR";
        }
        // 布尔常量
        if (value.equals("TRUE") || value.equals("FALSE")) {
            return "BOOLEAN";
        }
        // 数字常量 - 改进的数字识别
        try {
            // 尝试解析为整数
            Integer.parseInt(value);
            return "INT";
        } catch (NumberFormatException e1) {
            try {
                // 尝试解析为浮点数
                Float.parseFloat(value);
                return "FLOAT";
            } catch (NumberFormatException e2) {
                // 都不是数字
            }
        }
        
        // 使用正则表达式作为后备
        if (value.matches("\\d+")) {
            return "INT";
        }
        if (value.matches("\\d+\\.\\d+")) {
            return "FLOAT";
        }
        return "UNKNOWN";
    }
    
    /**
     * 检查是否为数值类型
     */
    private boolean isNumericType(String type) {
        return type.equals("INT") || type.equals("FLOAT") || type.equals("DOUBLE");
    }
    
    /**
     * 检查值与列类型是否兼容
     */
    private boolean isTypeCompatible(Object value, String columnType) {
        String valueType = getValueType(value);
        return valueType.equals(columnType) || 
               (isNumericType(valueType) && isNumericType(columnType));
    }
    
    /**
     * 获取值的类型
     */
    private String getValueType(Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (str.startsWith("'") && str.endsWith("'")) {
                return "VARCHAR";
            }
            if (str.equals("TRUE") || str.equals("FALSE")) {
                return "BOOLEAN";
            }
            // 对于纯数字字符串，推断为数字类型
            return inferConstantType(str);
        }
        if (value instanceof Integer) {
            return "INT";
        }
        if (value instanceof Float || value instanceof Double) {
            return "FLOAT";
        }
        return "UNKNOWN";
    }
    
    /**
     * 生成新的临时变量名
     */
    private String newTempVar() {
        return "t" + (tempVarCounter++);
    }
    
    /**
     * 添加四元式
     */
    private void addQuadruple(String op, String arg1, String arg2, String result) {
        quadruples.add(new Quadruple(op, arg1, arg2, result));
    }
    
    /**
     * 添加错误信息
     */
    private void addError(String error) {
        errors.add(error);
    }
    
    /**
     * 获取生成的四元式
     */
    public List<Quadruple> getQuadruples() {
        return new ArrayList<>(quadruples);
    }
    
    /**
     * 获取错误列表
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 检查是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
