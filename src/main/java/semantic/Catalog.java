package semantic;

import java.util.*;

/**
 * 数据库目录 - 管理表和列的元数据
 */
public class Catalog {
    private Map<String, TableSchema> tables;
    
    public Catalog() {
        this.tables = new HashMap<>();
        initializeSystemTables();
    }
    
    /**
     * 初始化系统表（用于测试）
     */
    private void initializeSystemTables() {
        // 创建用户表
        List<ColumnSchema> userColumns = new ArrayList<>();
        userColumns.add(new ColumnSchema("id", "INT", false));
        userColumns.add(new ColumnSchema("name", "VARCHAR", false));
        userColumns.add(new ColumnSchema("age", "INT", true));
        userColumns.add(new ColumnSchema("status", "VARCHAR", true));
        userColumns.add(new ColumnSchema("role", "VARCHAR", true));
        TableSchema usersTable = new TableSchema("users", userColumns);
        tables.put("users", usersTable);
        
        // 创建产品表
        List<ColumnSchema> productColumns = new ArrayList<>();
        productColumns.add(new ColumnSchema("id", "INT", false));
        productColumns.add(new ColumnSchema("name", "VARCHAR", false));
        productColumns.add(new ColumnSchema("price", "FLOAT", true));
        productColumns.add(new ColumnSchema("available", "BOOLEAN", true));
        productColumns.add(new ColumnSchema("date", "DATE", true));
        TableSchema productTable = new TableSchema("product", productColumns);
        tables.put("product", productTable);
    }
    
    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }
    
    /**
     * 获取表结构
     */
    public TableSchema getTable(String tableName) {
        return tables.get(tableName.toLowerCase());
    }
    
    /**
     * 创建新表（从列结构列表）
     */
    public void createTable(String tableName, List<ColumnSchema> columns) {
        TableSchema table = new TableSchema(tableName, columns);
        tables.put(tableName.toLowerCase(), table);
    }
    
    /**
     * 添加表到目录
     */
    public void addTable(String tableName, TableSchema table) {
        tables.put(tableName.toLowerCase(), table);
    }
    
    /**
     * 删除表
     */
    public boolean dropTable(String tableName) {
        return tables.remove(tableName.toLowerCase()) != null;
    }
    
    /**
     * 检查列是否存在
     */
    public boolean columnExists(String tableName, String columnName) {
        TableSchema table = getTable(tableName);
        return table != null && table.hasColumn(columnName);
    }
    
    /**
     * 获取所有表名
     */
    public Set<String> getAllTableNames() {
        return new HashSet<>(tables.keySet());
    }
    
    /**
     * 获取表的列信息
     */
    public List<ColumnSchema> getTableColumns(String tableName) {
        TableSchema table = getTable(tableName);
        return table != null ? table.getColumns() : new ArrayList<>();
    }
    
    /**
     * 清空目录（用于测试）
     */
    public void clear() {
        tables.clear();
    }
    
    /**
     * 获取目录状态信息
     */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 数据库目录状态 ===\n");
        sb.append("表数量: ").append(tables.size()).append("\n\n");
        
        for (Map.Entry<String, TableSchema> entry : tables.entrySet()) {
            TableSchema table = entry.getValue();
            sb.append("表名: ").append(table.getTableName()).append("\n");
            sb.append("列数: ").append(table.getColumns().size()).append("\n");
            sb.append("列信息:\n");
            for (ColumnSchema col : table.getColumns()) {
                sb.append("  - ").append(col.getColumnName()).append(" (").append(col.getDataType()).append(")");
                if (!col.isNullable()) {
                    sb.append(" NOT NULL");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "Catalog{tables=" + tables.keySet() + "}";
    }
}
