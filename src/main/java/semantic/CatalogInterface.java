package semantic;

import executor.common.TableSchema;
import java.util.List;

/**
 * 目录接口 - 为SQL编译器提供表和列的元数据访问
 * 这个接口将被实现为适配器，连接到executor模块的CatalogManager
 */
public interface CatalogInterface {
    
    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 如果表存在返回true
     */
    boolean tableExists(String tableName);
    
    /**
     * 获取表结构信息
     * @param tableName 表名
     * @return 表结构信息，如果表不存在返回null
     */
    TableMetadata getTable(String tableName);
    
    /**
     * 检查列是否存在于指定表中
     * @param tableName 表名
     * @param columnName 列名
     * @return 如果列存在返回true
     */
    boolean columnExists(String tableName, String columnName);
    
    /**
     * 获取表的所有列信息
     * @param tableName 表名
     * @return 列信息列表
     */
    List<ColumnMetadata> getTableColumns(String tableName);
    
    /**
     * 获取所有表名
     * @return 表名集合
     */
    List<String> getAllTableNames();
    
    /**
     * 验证数据类型是否有效
     * @param dataType 数据类型字符串
     * @return 如果数据类型有效返回true
     */
    boolean isValidDataType(String dataType);
    
    /**
     * 注册新表到系统目录
     * @param tableName 表名
     * @param schema 表结构
     */
    void registerTable(String tableName, TableSchema schema);
}
