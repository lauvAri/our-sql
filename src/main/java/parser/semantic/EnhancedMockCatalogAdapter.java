package parser.semantic;

import executor.common.TableSchema;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * 增强的模拟目录适配器，确保表注册和查找功能正常工作
 */
public class EnhancedMockCatalogAdapter implements CatalogInterface {
    
    // 存储已注册的表名
    private Set<String> registeredTables = new HashSet<>();
    
    // 存储动态创建的表的列信息
    private Map<String, List<ColumnMetadata>> dynamicTableColumns = new HashMap<>();
    
    public void addTable(String tableName) {
        registeredTables.add(tableName.toLowerCase());
        System.out.println("✅ 模拟注册表: " + tableName);
    }
    
    /**
     * 添加表及其列信息
     */
    public void addTable(String tableName, List<ColumnMetadata> columns) {
        registeredTables.add(tableName.toLowerCase());
        dynamicTableColumns.put(tableName.toLowerCase(), columns);
        System.out.println("✅ 模拟注册表: " + tableName + " (包含 " + columns.size() + " 列)");
    }
    
    @Override
    public boolean tableExists(String tableName) {
        boolean exists = registeredTables.contains(tableName.toLowerCase());
        System.out.println("🔍 检查表 " + tableName + " 是否存在: " + (exists ? "是" : "否"));
        return exists;
    }
    
    @Override
    public TableMetadata getTable(String tableName) {
        if (tableExists(tableName)) {
            // 获取表的列信息并创建完整的表元数据
            List<ColumnMetadata> columns = getTableColumns(tableName);
            return new TableMetadata(tableName, columns);
        }
        return null;
    }
    
    @Override
    public void registerTable(String tableName, TableSchema schema) {
        addTable(tableName);
    }
    
    @Override
    public boolean columnExists(String tableName, String columnName) {
        // 如果表存在，假设常见列存在
        if (tableExists(tableName)) {
            // 为演示目的，假设这些常见列存在
            return columnName.equals("id") || columnName.equals("username") || 
                   columnName.equals("email") || columnName.equals("name") || 
                   columnName.equals("pid") || columnName.equals("price");
        }
        return false;
    }
    
    @Override
    public List<ColumnMetadata> getTableColumns(String tableName) {
        List<ColumnMetadata> columns = new ArrayList<>();
        if (tableExists(tableName)) {
            String lowerTableName = tableName.toLowerCase();
            
            // 首先检查动态创建的表
            if (dynamicTableColumns.containsKey(lowerTableName)) {
                columns.addAll(dynamicTableColumns.get(lowerTableName));
            } else {
                // 为预定义的表返回具体的列信息
                if ("users".equals(lowerTableName)) {
                    columns.add(new ColumnMetadata("id", "INT", false, true, 4));
                    columns.add(new ColumnMetadata("username", "VARCHAR", false, false, 50));
                    columns.add(new ColumnMetadata("email", "VARCHAR", false, false, 100));
                } else if ("products".equals(lowerTableName)) {
                    columns.add(new ColumnMetadata("pid", "INT", false, true, 4));
                    columns.add(new ColumnMetadata("name", "VARCHAR", false, false, 100));
                    columns.add(new ColumnMetadata("price", "VARCHAR", false, false, 20));
                }
            }
        }
        return columns;
    }
    
    @Override
    public List<String> getAllTableNames() {
        return new ArrayList<>(registeredTables);
    }
    
    @Override
    public boolean isValidDataType(String dataType) {
        // 支持基本数据类型
        return dataType.equalsIgnoreCase("INT") || 
               dataType.toUpperCase().startsWith("VARCHAR");
    }
}