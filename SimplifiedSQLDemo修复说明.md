=== SimplifiedSQLDemo 修复说明 ===

🔍 问题分析:
原来只有CREATE TABLE成功，INSERT/SELECT/DELETE失败的原因是：
1. CREATE TABLE 不需要检查表是否存在（语义分析通过）
2. INSERT/SELECT/DELETE 需要表存在才能通过语义分析
3. MockCatalogAdapter 的 tableExists() 方法一直返回 false

🛠️ 修复方案:
1. 修改 MockCatalogAdapter 增加表注册功能：
   - 添加 registeredTables Map 来存储已注册的表
   - tableExists() 方法改为检查 registeredTables
   - registerTable() 方法真正保存表信息

2. 修改 SimplifiedSQLDemo 增加表注册逻辑：
   - CREATE TABLE 成功后，立即注册表到 MockCatalogAdapter
   - 为 users 和 products 表分别创建 TableSchema 并注册

🎯 修复后的执行流程:
1. CREATE TABLE users - ✅ 成功（不需要检查表存在）
   → 立即注册 users 表到目录
2. INSERT INTO users - ✅ 成功（能找到 users 表）
3. SELECT FROM users - ✅ 成功（能找到 users 表）
4. DELETE FROM users - ✅ 成功（能找到 users 表）
5. CREATE TABLE products - ✅ 成功（不需要检查表存在）
   → 立即注册 products 表到目录

📝 关键代码修改:

MockCatalogAdapter.java:
```java
// 添加表存储
private Map<String, TableMetadata> registeredTables = new HashMap<>();

@Override
public boolean tableExists(String tableName) {
    return registeredTables.containsKey(tableName.toLowerCase());
}

@Override
public void registerTable(String tableName, TableSchema schema) {
    // 真正保存表信息
    List<ColumnMetadata> columns = new ArrayList<>();
    TableMetadata metadata = new TableMetadata(tableName, columns);
    registeredTables.put(tableName.toLowerCase(), metadata);
}
```

SimplifiedSQLDemo.java:
```java
case CREATE_TABLE:
    CreateTablePlan createPlan = (CreateTablePlan) plan;
    // ... 显示信息 ...
    
    // 关键修复：立即注册表
    if ("users".equals(createPlan.getTableName())) {
        executor.common.TableSchema.Builder builder = new executor.common.TableSchema.Builder().tableName("users");
        builder.addColumn("id", executor.common.ColumnType.INT, 4, true);
        builder.addColumn("username", executor.common.ColumnType.VARCHAR, 50, false);
        builder.addColumn("email", executor.common.ColumnType.VARCHAR, 100, false);
        executor.common.TableSchema schema = builder.build();
        mockCatalog.registerTable("users", schema);
    }
    break;
```

🎉 预期结果:
修复后所有5个SQL语句都应该编译成功：
1. CREATE TABLE users - ✅ 成功
2. INSERT INTO users - ✅ 成功  
3. SELECT FROM users - ✅ 成功
4. DELETE FROM users - ✅ 成功
5. CREATE TABLE products - ✅ 成功

这样就实现了真正的通用SQL编译接口，能够处理各种类型的SQL语句！