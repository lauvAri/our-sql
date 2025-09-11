package semantic;

import java.util.*;

/**
 * 表结构定义
 */
public class TableSchema {
    private String tableName;
    private List<ColumnSchema> columns;
    private Map<String, ColumnSchema> columnMap;  // 用于快速查找列
    
    public TableSchema(String tableName, List<ColumnSchema> columns) {
        this.tableName = tableName;
        this.columns = new ArrayList<>(columns);
        this.columnMap = new HashMap<>();
        
        // 构建列名映射
        for (ColumnSchema column : columns) {
            columnMap.put(column.getColumnName().toLowerCase(), column);
        }
    }
    
    // Getter方法
    public String getTableName() { return tableName; }
    public List<ColumnSchema> getColumns() { return new ArrayList<>(columns); }
    
    /**
     * 检查列是否存在
     */
    public boolean hasColumn(String columnName) {
        return columnMap.containsKey(columnName.toLowerCase());
    }
    
    /**
     * 获取列结构
     */
    public ColumnSchema getColumn(String columnName) {
        return columnMap.get(columnName.toLowerCase());
    }
    
    /**
     * 获取所有列名
     */
    public List<String> getColumnNames() {
        List<String> names = new ArrayList<>();
        for (ColumnSchema column : columns) {
            names.add(column.getColumnName());
        }
        return names;
    }
    
    /**
     * 获取主键列
     */
    public List<ColumnSchema> getPrimaryKeys() {
        List<ColumnSchema> primaryKeys = new ArrayList<>();
        for (ColumnSchema column : columns) {
            if (column.isPrimaryKey()) {
                primaryKeys.add(column);
            }
        }
        return primaryKeys;
    }
    
    /**
     * 获取可空列
     */
    public List<ColumnSchema> getNullableColumns() {
        List<ColumnSchema> nullableColumns = new ArrayList<>();
        for (ColumnSchema column : columns) {
            if (column.isNullable()) {
                nullableColumns.add(column);
            }
        }
        return nullableColumns;
    }
    
    /**
     * 获取非空列
     */
    public List<ColumnSchema> getNonNullableColumns() {
        List<ColumnSchema> nonNullableColumns = new ArrayList<>();
        for (ColumnSchema column : columns) {
            if (!column.isNullable()) {
                nonNullableColumns.add(column);
            }
        }
        return nonNullableColumns;
    }
    
    /**
     * 添加列
     */
    public void addColumn(ColumnSchema column) {
        if (!hasColumn(column.getColumnName())) {
            columns.add(column);
            columnMap.put(column.getColumnName().toLowerCase(), column);
        }
    }
    
    /**
     * 删除列
     */
    public boolean removeColumn(String columnName) {
        ColumnSchema column = columnMap.remove(columnName.toLowerCase());
        if (column != null) {
            columns.remove(column);
            return true;
        }
        return false;
    }
    
    /**
     * 获取列数量
     */
    public int getColumnCount() {
        return columns.size();
    }
    
    /**
     * 检查记录是否符合表结构
     */
    public boolean isRecordValid(Map<String, Object> record) {
        // 检查是否有未知列
        for (String columnName : record.keySet()) {
            if (!hasColumn(columnName)) {
                return false;
            }
        }
        
        // 检查必需的非空列
        for (ColumnSchema column : getNonNullableColumns()) {
            if (!record.containsKey(column.getColumnName()) || 
                record.get(column.getColumnName()) == null) {
                return false;
            }
        }
        
        // 检查类型兼容性
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            ColumnSchema column = getColumn(entry.getKey());
            if (!column.isValueCompatible(entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取表的DDL描述
     */
    public String getDDL() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        for (int i = 0; i < columns.size(); i++) {
            sb.append("    ").append(columns.get(i).toString());
            if (i < columns.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("TableSchema{name='%s', columns=%d}", tableName, columns.size());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TableSchema that = (TableSchema) obj;
        return tableName.equals(that.tableName);
    }
    
    @Override
    public int hashCode() {
        return tableName.hashCode();
    }
}
