package parser.semantic;

/**
 * 列结构定义
 */
public class ColumnSchema {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private Object defaultValue;
    private boolean primaryKey;
    
    public ColumnSchema(String columnName, String dataType, boolean nullable) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.defaultValue = null;
        this.primaryKey = false;
    }
    
    public ColumnSchema(String columnName, String dataType, boolean nullable, 
                       Object defaultValue, boolean primaryKey) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.primaryKey = primaryKey;
    }
    
    // Getter方法
    public String getColumnName() { return columnName; }
    public String getDataType() { return dataType; }
    public boolean isNullable() { return nullable; }
    public Object getDefaultValue() { return defaultValue; }
    public boolean isPrimaryKey() { return primaryKey; }
    
    // Setter方法
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
    
    /**
     * 检查值是否与列类型兼容
     */
    public boolean isValueCompatible(Object value) {
        if (value == null) {
            return nullable;
        }
        
        String valueType = getValueType(value);
        return isTypeCompatible(valueType, dataType);
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
     * 检查类型兼容性
     */
    private boolean isTypeCompatible(String valueType, String columnType) {
        if (valueType.equals(columnType)) {
            return true;
        }
        
        // 数值类型兼容性
        if (isNumericType(valueType) && isNumericType(columnType)) {
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(columnName).append(" ").append(dataType);
        if (!nullable) {
            sb.append(" NOT NULL");
        }
        if (primaryKey) {
            sb.append(" PRIMARY KEY");
        }
        if (defaultValue != null) {
            sb.append(" DEFAULT ").append(defaultValue);
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ColumnSchema that = (ColumnSchema) obj;
        return columnName.equals(that.columnName);
    }
    
    @Override
    public int hashCode() {
        return columnName.hashCode();
    }
}
