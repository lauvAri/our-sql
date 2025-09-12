package parser.semantic;

import executor.systemCatalog.CatalogManager;
import executor.common.ColumnDefinition;
import executor.common.TableSchema;
import executor.common.ColumnType;
import java.util.List;
import java.util.ArrayList;

/**
 * Catalog适配器 - 将executor模块的CatalogManager适配为SQL编译器使用的接口
 * 实现了适配器模式，提供了SQL编译器与executor模块之间的桥梁
 */
public class CatalogAdapter implements CatalogInterface {
    private final CatalogManager catalogManager;
    
    public CatalogAdapter(CatalogManager catalogManager) {
        this.catalogManager = catalogManager;
    }
    
    /**
     * 检查表是否存在
     */
    @Override
    public boolean tableExists(String tableName) {
        return catalogManager.tableExists(tableName);
    }
    
    /**
     * 获取表元数据
     */
    @Override
    public TableMetadata getTable(String tableName) {
        try {
            if (!catalogManager.tableExists(tableName)) {
                return null;
            }
            
            TableSchema tableSchema = catalogManager.getTableSchema(tableName);
            if (tableSchema == null) {
                return null;
            }
            
            List<ColumnMetadata> columns = convertColumns(tableSchema.columns());
            return new TableMetadata(tableName, columns);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 检查表中的列是否存在
     */
    @Override
    public boolean columnExists(String tableName, String columnName) {
        TableMetadata table = getTable(tableName);
        return table != null && table.hasColumn(columnName);
    }
    
    /**
     * 获取表的所有列信息
     */
    @Override
    public List<ColumnMetadata> getTableColumns(String tableName) {
        TableMetadata table = getTable(tableName);
        if (table != null) {
            return table.getColumns();
        }
        return new ArrayList<>();
    }
    
    /**
     * 获取所有表名
     */
    @Override
    public List<String> getAllTableNames() {
        // 由于CatalogManager目前没有getAllTableNames方法，我们返回空列表
        // 这个方法需要在executor模块中实现
        return new ArrayList<>();
    }
    
    /**
     * 转换executor模块的列定义为SQL编译器的列元数据
     */
    private List<ColumnMetadata> convertColumns(List<ColumnDefinition> columnDefinitions) {
        List<ColumnMetadata> columns = new ArrayList<>();
        
        for (ColumnDefinition columnDef : columnDefinitions) {
            String columnName = columnDef.name();
            String dataType = convertColumnType(columnDef.type());
            boolean nullable = true; // 默认可为空，因为ColumnDefinition没有nullable字段
            boolean primaryKey = columnDef.isPrimaryKey();
            int maxLength = columnDef.length();
            
            ColumnMetadata columnMetadata = new ColumnMetadata(
                columnName, dataType, nullable, primaryKey, maxLength
            );
            columns.add(columnMetadata);
        }
        
        return columns;
    }
    
    /**
     * 转换ColumnType为字符串
     */
    private String convertColumnType(ColumnType columnType) {
        switch (columnType) {
            case INT:
                return "INT";
            case VARCHAR:
                return "VARCHAR";
            case FLOAT:
                return "FLOAT";
            case BOOLEAN:
                return "BOOLEAN";
            case TIMESTAMP:
                return "TIMESTAMP";
            default:
                return "VARCHAR";
        }
    }
    
    /**
     * 检查数据类型是否有效
     */
    @Override
    public boolean isValidDataType(String dataType) {
        if (dataType == null) {
            return false;
        }
        
        String upperType = dataType.toUpperCase();
        return upperType.equals("INT") || upperType.equals("INTEGER") ||
               upperType.equals("VARCHAR") || upperType.equals("CHAR") ||
               upperType.equals("FLOAT") || upperType.equals("DOUBLE") ||
               upperType.equals("BOOLEAN") || upperType.equals("DATE") ||
               upperType.equals("DATETIME") || upperType.equals("TEXT");
    }
    
    /**
     * 获取数据类型的默认长度
     */
    public int getDefaultLength(String dataType) {
        if (dataType == null) {
            return -1;
        }
        
        switch (dataType.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return 4;
            case "FLOAT":
                return 4;
            case "DOUBLE":
                return 8;
            case "BOOLEAN":
                return 1;
            case "VARCHAR":
                return 255;
            case "CHAR":
                return 1;
            default:
                return -1;
        }
    }
    
    /**
     * 注册新表到系统目录
     */
    @Override
    public void registerTable(String tableName, TableSchema schema) {
        catalogManager.registerTable(tableName, schema);
    }
}
