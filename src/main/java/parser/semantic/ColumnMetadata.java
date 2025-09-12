package parser.semantic;

/**
 * 列元数据 - 用于SQL编译器的列信息表示
 * 这是一个轻量级的数据传输对象，从executor模块的列信息转换而来
 */
public class ColumnMetadata {
    private final String columnName;
    private final String dataType;
    private final boolean nullable;
    private final boolean primaryKey;
    private final int maxLength;
    
    public ColumnMetadata(String columnName, String dataType, boolean nullable) {
        this(columnName, dataType, nullable, false, -1);
    }
    
    public ColumnMetadata(String columnName, String dataType, boolean nullable, 
                         boolean primaryKey, int maxLength) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.maxLength = maxLength;
    }
    
    /**
     * 获取列名
     */
    public String getColumnName() {
        return columnName;
    }
    
    /**
     * 获取数据类型
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * 是否可为空
     */
    public boolean isNullable() {
        return nullable;
    }
    
    /**
     * 是否为主键
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }
    
    /**
     * 获取最大长度
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * 检查值是否与列类型兼容
     */
    public boolean isValueCompatible(Object value) {
        if (value == null) {
            return nullable;
        }
        
        String valueType = inferValueType(value);
        return isTypeCompatible(valueType, dataType);
    }
    
    /**
     * 推断值的类型
     */
    private String inferValueType(Object value) {
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
        return "INT".equals(type) || "FLOAT".equals(type) || "DOUBLE".equals(type);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(columnName).append("(").append(dataType);
        if (maxLength > 0) {
            sb.append(",").append(maxLength);
        }
        sb.append(")");
        if (!nullable) {
            sb.append(" NOT NULL");
        }
        if (primaryKey) {
            sb.append(" PK");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ColumnMetadata that = (ColumnMetadata) obj;
        return columnName.equalsIgnoreCase(that.columnName);
    }
    
    @Override
    public int hashCode() {
        return columnName.toLowerCase().hashCode();
    }
}
