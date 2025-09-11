package semantic;

/**
 * 语义错误类
 * 格式: [错误类型，位置，原因说明]
 */
public class SemanticError {
    public enum ErrorType {
        TABLE_NOT_FOUND,        // 表不存在
        COLUMN_NOT_FOUND,       // 列不存在
        TYPE_MISMATCH,          // 类型不匹配
        DUPLICATE_COLUMN,       // 重复列名
        INVALID_DATA_TYPE,      // 无效数据类型
        TABLE_ALREADY_EXISTS,   // 表已存在
        COLUMN_COUNT_MISMATCH,  // 列数不匹配
        SYNTAX_ERROR           // 语法错误
    }
    
    private ErrorType errorType;
    private String position;
    private String description;
    
    public SemanticError(ErrorType errorType, String position, String description) {
        this.errorType = errorType;
        this.position = position;
        this.description = description;
    }
    
    // Getter方法
    public ErrorType getErrorType() { return errorType; }
    public String getPosition() { return position; }
    public String getDescription() { return description; }
    
    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", errorType, position, description);
    }
    
    /**
     * 创建表不存在错误
     */
    public static SemanticError tableNotFound(String tableName, String position) {
        return new SemanticError(ErrorType.TABLE_NOT_FOUND, position, 
                               "表 '" + tableName + "' 不存在");
    }
    
    /**
     * 创建列不存在错误
     */
    public static SemanticError columnNotFound(String columnName, String tableName, String position) {
        return new SemanticError(ErrorType.COLUMN_NOT_FOUND, position, 
                               "列 '" + columnName + "' 在表 '" + tableName + "' 中不存在");
    }
    
    /**
     * 创建类型不匹配错误
     */
    public static SemanticError typeMismatch(String columnName, String expectedType, 
                                           String actualType, String position) {
        return new SemanticError(ErrorType.TYPE_MISMATCH, position, 
                               "列 '" + columnName + "' 类型不匹配，期望 " + expectedType + 
                               "，实际 " + actualType);
    }
    
    /**
     * 创建重复列名错误
     */
    public static SemanticError duplicateColumn(String columnName, String position) {
        return new SemanticError(ErrorType.DUPLICATE_COLUMN, position, 
                               "列名 '" + columnName + "' 重复");
    }
    
    /**
     * 创建无效数据类型错误
     */
    public static SemanticError invalidDataType(String dataType, String position) {
        return new SemanticError(ErrorType.INVALID_DATA_TYPE, position, 
                               "无效的数据类型: " + dataType);
    }
    
    /**
     * 创建表已存在错误
     */
    public static SemanticError tableAlreadyExists(String tableName, String position) {
        return new SemanticError(ErrorType.TABLE_ALREADY_EXISTS, position, 
                               "表 '" + tableName + "' 已存在");
    }
    
    /**
     * 创建列数不匹配错误
     */
    public static SemanticError columnCountMismatch(int expectedCount, int actualCount, String position) {
        return new SemanticError(ErrorType.COLUMN_COUNT_MISMATCH, position, 
                               "列数不匹配，期望 " + expectedCount + "，实际 " + actualCount);
    }
}
