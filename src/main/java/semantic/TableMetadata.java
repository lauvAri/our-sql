package semantic;

import java.util.List;
import java.util.ArrayList;

/**
 * 表元数据 - 用于SQL编译器的表信息表示
 * 这是一个轻量级的数据传输对象，从executor模块的TableSchema转换而来
 */
public class TableMetadata {
    private final String tableName;
    private final List<ColumnMetadata> columns;
    
    public TableMetadata(String tableName, List<ColumnMetadata> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }
    
    /**
     * 获取表名
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * 获取所有列
     */
    public List<ColumnMetadata> getColumns() {
        return columns;
    }
    
    /**
     * 检查列是否存在
     */
    public boolean hasColumn(String columnName) {
        return columns.stream()
                .anyMatch(col -> col.getColumnName().equalsIgnoreCase(columnName));
    }
    
    /**
     * 获取指定列的信息
     */
    public ColumnMetadata getColumn(String columnName) {
        return columns.stream()
                .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取列数量
     */
    public int getColumnCount() {
        return columns.size();
    }
    
    /**
     * 获取所有列名
     */
    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        for (ColumnMetadata column : columns) {
            columnNames.add(column.getColumnName());
        }
        return columnNames;
    }
    
    @Override
    public String toString() {
        return String.format("TableMetadata{name='%s', columns=%d}", tableName, columns.size());
    }
}
