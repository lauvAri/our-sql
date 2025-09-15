# SQL编译器使用手册

## 概述

本SQL编译器是一个完整的SQL语句编译系统，能够将SQL语句转换为LogicalPlan子类对象，支持词法分析、语法分析、语义分析和执行计划生成的完整流程。

## 架构设计

### 整体架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   SQL输入       │───▶│  SQL编译器      │───▶│ LogicalPlan子类 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │
                               ▼
                    ┌─────────────────────────┐
                    │    编译流程             │
                    │  1. 词法分析(Lexer)      │
                    │  2. 语法分析(Parser)     │
                    │  3. 语义分析(Semantic)   │
                    │  4. 计划生成(Plan)       │
                    └─────────────────────────┘
```

### 核心组件
- **SQLCompiler**: 统一的编译器接口
- **SQLLexer**: 词法分析器，将SQL文本分解为Token
- **SQLParser**: 语法分析器，生成抽象语法树(AST)
- **EnhancedSemanticAnalyzer**: 语义分析器，进行语义验证
- **PlanGenerator**: 执行计划生成器
- **CatalogAdapter**: 适配器模式，集成executor模块

## 工作流程

### 1. 编译流程
```
SQL文本 → 词法分析 → Token流 → 语法分析 → AST → 语义分析 → 执行计划生成 → LogicalPlan
```

### 2. 详细步骤
1. **词法分析**: 将SQL文本分解为关键字、标识符、常量等Token
2. **语法分析**: 根据SQL语法规则构建抽象语法树
3. **语义分析**: 验证表存在性、列名正确性、类型匹配等
4. **计划生成**: 根据SQL操作类型生成相应的LogicalPlan子类

### 3. 支持的SQL操作
- **SELECT**: 查询操作 → `SelectPlan`
- **INSERT**: 插入操作 → `InsertPlan`
- **DELETE**: 删除操作 → `DeletePlan`
- **CREATE TABLE**: 建表操作 → `CreateTablePlan`

## 核心接口说明

### SQLCompiler类

#### 构造函数
```java
public SQLCompiler(CatalogInterface catalogInterface)
```
- **参数**: `catalogInterface` - 系统目录接口，通常使用`CatalogAdapter`

#### 核心方法

##### 1. 通用编译方法
```java
public LogicalPlan compile(String sql) throws SQLCompilerException
```
- **功能**: 将任意SQL语句编译为对应的LogicalPlan子类
- **参数**: `sql` - SQL语句字符串
- **返回值**: LogicalPlan子类对象
- **异常**: SQLCompilerException - 编译失败时抛出

**使用示例**:
```java
SQLCompiler compiler = new SQLCompiler(catalogAdapter);
LogicalPlan plan = compiler.compile("SELECT * FROM users;");
```

##### 2. 特定类型编译方法

**编译SELECT语句**:
```java
public SelectPlan compileSelect(String sql) throws SQLCompilerException
```
- **功能**: 专门编译SELECT语句
- **返回值**: SelectPlan对象

**编译INSERT语句**:
```java
public InsertPlan compileInsert(String sql) throws SQLCompilerException
```
- **功能**: 专门编译INSERT语句  
- **返回值**: InsertPlan对象

**编译DELETE语句**:
```java
public DeletePlan compileDelete(String sql) throws SQLCompilerException
```
- **功能**: 专门编译DELETE语句
- **返回值**: DeletePlan对象

**编译CREATE TABLE语句**:
```java
public CreateTablePlan compileCreateTable(String sql) throws SQLCompilerException
```
- **功能**: 专门编译CREATE TABLE语句
- **返回值**: CreateTablePlan对象

##### 3. 验证方法
```java
public ValidationResult validate(String sql)
```
- **功能**: 仅验证SQL语法和语义正确性，不生成执行计划
- **参数**: `sql` - SQL语句
- **返回值**: ValidationResult对象，包含验证结果和错误信息

##### 4. 调试方法
```java
public ASTNode getAST(String sql) throws SQLCompilerException
```
- **功能**: 获取SQL语句的抽象语法树，用于调试
- **返回值**: AST根节点

```java
public EnhancedSemanticAnalyzer getSemanticAnalyzer()
```
- **功能**: 获取语义分析器实例，用于高级功能

## LogicalPlan子类详解

### 1. SelectPlan
```java
public class SelectPlan extends LogicalPlan {
    public String getTableName();      // 查询的表名
    public List<String> getColumns();  // 选择的列名列表
    public Expression getFilter();     // WHERE条件表达式
}
```

### 2. InsertPlan  
```java
public class InsertPlan extends LogicalPlan {
    public String getTableName();      // 插入的表名
    public List<List<Object>> getValues(); // 插入的值列表
}
```

### 3. DeletePlan
```java
public class DeletePlan extends LogicalPlan {
    public String getTableName();      // 删除的表名
    public Expression getFilter();     // WHERE条件表达式
}
```

### 4. CreateTablePlan
```java
public class CreateTablePlan extends LogicalPlan {
    public String getTableName();      // 创建的表名
    public List<Column> getColumns();  // 表的列定义列表
}
```

## 使用示例

### 1. 基本初始化
```java
// 初始化存储服务和引擎
StorageService storageService = new StorageService("test_db.db", "test_idx.idx");
StorageEngine storageEngine = new StorageEngineImpl(storageService);

// 创建目录管理器和适配器
CatalogManager catalogManager = new CatalogManager(storageEngine);
CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);

// 创建SQL编译器
SQLCompiler compiler = new SQLCompiler(catalogAdapter);
```

### 2. 编译不同类型的SQL语句

**编译CREATE TABLE**:
```java
String createSQL = "CREATE TABLE users (id INT, name VARCHAR, age INT);";
CreateTablePlan createPlan = compiler.compileCreateTable(createSQL);
System.out.println("表名: " + createPlan.getTableName());
System.out.println("列数: " + createPlan.getColumns().size());
```

**编译SELECT**:
```java
String selectSQL = "SELECT id, name FROM users WHERE age > 18;";
SelectPlan selectPlan = compiler.compileSelect(selectSQL);
System.out.println("查询表: " + selectPlan.getTableName());
System.out.println("选择列: " + selectPlan.getColumns());
System.out.println("过滤条件: " + selectPlan.getFilter());
```

**编译INSERT**:
```java
String insertSQL = "INSERT INTO users (id, name, age) VALUES (1, 'John', 25);";
InsertPlan insertPlan = compiler.compileInsert(insertSQL);
System.out.println("插入表: " + insertPlan.getTableName());
System.out.println("插入值: " + insertPlan.getValues());
```

**编译DELETE**:
```java
String deleteSQL = "DELETE FROM users WHERE id = 1;";
DeletePlan deletePlan = compiler.compileDelete(deleteSQL);
System.out.println("删除表: " + deletePlan.getTableName());
System.out.println("删除条件: " + deletePlan.getFilter());
```

### 3. 错误处理
```java
try {
    LogicalPlan plan = compiler.compile("SELECT * FROM nonexistent_table;");
} catch (SQLCompilerException e) {
    System.out.println("编译失败: " + e.getMessage());
    System.out.println("SQL: " + e.getSql());
}
```

### 4. SQL验证
```java
ValidationResult result = compiler.validate("SELECT id, name FROM users;");
if (result.isSuccess()) {
    System.out.println("✅ SQL语法和语义正确");
} else {
    System.out.println("❌ SQL验证失败: " + result.getMessage());
}
```

## 测试工具

### 1. SQLCompilerInterfaceDemo
完整的接口演示类，包含：
- 各种SQL类型的编译演示
- 错误处理演示
- 验证功能演示

**运行方式**:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="semantic.SQLCompilerInterfaceDemo"
```

### 2. Main类（综合测试）
原有的综合测试类，包含：
- 完整的编译流程测试
- 执行计划格式化输出
- LogicalPlan子类对象验证

**运行方式**:
```bash
mvn compile
mvn exec:java
```

## 高级功能

### 1. 执行计划格式化
```java
// 获取语义分析器进行高级操作
EnhancedSemanticAnalyzer analyzer = compiler.getSemanticAnalyzer();
PlanGenerationResult result = analyzer.generatePlan(ast);

// 格式化输出
PlanFormatter formatter = new PlanFormatter();
System.out.println("树形格式:");
System.out.println(formatter.format(result.getPlan(), PlanFormatter.OutputFormat.TREE));

System.out.println("JSON格式:");
System.out.println(formatter.format(result.getPlan(), PlanFormatter.OutputFormat.JSON));
```

### 2. 语义分析详细信息
```java
EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult analysisResult = 
    compiler.getSemanticAnalyzer().analyze(ast);
    
System.out.println("语义分析结果:");
System.out.println(analysisResult.getFormattedResult());
```

## 异常处理

### SQLCompilerException
```java
public class SQLCompilerException extends Exception {
    public String getSql();     // 获取出错的SQL语句
    public String getMessage(); // 获取错误信息
}
```

### 常见异常类型
1. **语法错误**: SQL语句不符合语法规则
2. **语义错误**: 表不存在、列名错误等
3. **类型不匹配**: 期望的SQL类型与实际类型不符

## 配置和扩展

### 1. 自定义CatalogInterface
```java
public class CustomCatalog implements CatalogInterface {
    @Override
    public boolean tableExists(String tableName) {
        // 自定义实现
    }
    
    @Override
    public TableMetadata getTable(String tableName) {
        // 自定义实现
    }
}
```

### 2. 扩展新的SQL操作类型
1. 创建新的LogicalPlan子类
2. 在PlanGenerator中添加生成逻辑
3. 在SQLCompiler中添加专用编译方法

## 性能考虑

1. **内存管理**: AST和Plan对象及时释放
2. **缓存优化**: 可考虑缓存已编译的计划
3. **并发安全**: SQLCompiler是线程安全的

## 总结

本SQL编译器提供了完整的SQL编译功能，通过清晰的接口设计和适配器模式，实现了与底层存储引擎的解耦。开发者可以方便地使用`SQLCompiler`类进行各种SQL操作的编译，获得相应的LogicalPlan子类对象用于后续执行。

系统具有良好的错误处理机制、丰富的测试工具和扩展能力，可以满足各种SQL编译需求。
