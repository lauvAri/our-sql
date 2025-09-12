# SQL编译器 - 语义分析器与执行计划生成器完整实现

## 概述

本项目实现了一个完整的SQL编译器模块，包含增强的语义分析器和执行计划生成器。系统能够对SQL语句进行全面的语义分析，生成四元式中间代码，并将AST转换为逻辑执行计划。

## 核心组件

### 1. 语义分析器 (SemanticAnalyzer)
- **功能**: 对AST进行语义分析并生成四元式中间代码
- **检查类型**: 
  - 表存在性检查
  - 列存在性检查
  - 类型一致性检查
  - 列数/列序检查
- **输出**: 四元式序列 `[操作符, 操作数1, 操作数2, 结果]`

### 2. 执行计划生成器 (PlanGenerator)
- **功能**: 将AST转换为逻辑执行计划
- **支持的算子**: CreateTable、Insert、SeqScan、Filter、Project、Delete
- **输出格式**: 树形结构、JSON、S表达式
- **错误处理**: `[错误类型，位置，原因说明]` 格式

### 3. 增强语义分析器 (EnhancedSemanticAnalyzer)
- **功能**: 结合语义分析和执行计划生成的综合分析器
- **工作流程**: 语义分析 → 执行计划生成 → 综合结果报告

### 4. 数据库目录 (Catalog)
- **功能**: 维护表和列的元数据
- **特性**: 支持动态表创建、列验证、类型检查

## 错误处理系统

### 错误类型 (SemanticError)
- `TABLE_NOT_FOUND`: 表不存在
- `COLUMN_NOT_FOUND`: 列不存在  
- `TYPE_MISMATCH`: 类型不匹配
- `DUPLICATE_COLUMN`: 重复列名
- `INVALID_DATA_TYPE`: 无效数据类型
- `TABLE_ALREADY_EXISTS`: 表已存在
- `COLUMN_COUNT_MISMATCH`: 列数不匹配
- `SYNTAX_ERROR`: 语法错误

### 错误格式
```
[错误类型, 位置, 原因说明]
例如: [TABLE_NOT_FOUND, FROM, 表 'nonexistent' 不存在]
```

## 执行计划输出格式

### 1. 树形格式
```
├─ SELECT
│  ├─ SeqScan: users
│  ├─ Filter: (age > '18')
│  └─ Project: [id, name]
```

### 2. JSON格式
```json
{
  "operator": "SELECT",
  "children": [
    {
      "operator": "SeqScan",
      "table": "users"
    },
    {
      "operator": "Filter", 
      "condition": "(age > '18')"
    },
    {
      "operator": "Project",
      "columns": ["id", "name"]
    }
  ]
}
```

### 3. S表达式格式
```lisp
(select (project (id name)) (filter (age > '18') (seq-scan users)))
```

## 支持的SQL语句

### SELECT语句
- **语义检查**: 表存在性、列存在性、WHERE子句类型检查
- **执行计划**: SeqScan → Filter → Project
- **四元式**: SCAN、比较操作、FILTER、PROJECT

### CREATE TABLE语句
- **语义检查**: 表重复性、列重复性、数据类型有效性
- **执行计划**: CreateTable
- **四元式**: CREATE_TABLE

### INSERT语句
- **语义检查**: 表存在性、列存在性、类型兼容性、列数匹配
- **执行计划**: Insert
- **四元式**: INSERT

### DELETE语句
- **语义检查**: 表存在性、WHERE子句验证
- **执行计划**: SeqScan → Filter
- **四元式**: 条件计算、DELETE

## 类型系统

### 支持的数据类型
- `INT`: 整数类型
- `VARCHAR`: 字符串类型
- `FLOAT`/`DOUBLE`: 浮点数类型
- `BOOLEAN`: 布尔类型
- `DATE`: 日期类型
- `CHAR`: 字符类型

### 类型兼容性规则
1. 完全匹配: 相同类型直接兼容
2. 数值兼容: INT、FLOAT、DOUBLE之间可兼容
3. 智能推断: 字符串字面量根据内容推断类型

## 使用示例

### 基本用法
```java
// 创建数据库目录和分析器
Catalog catalog = new Catalog();
EnhancedSemanticAnalyzer analyzer = new EnhancedSemanticAnalyzer(catalog);

// 解析SQL
SQLLexer lexer = new SQLLexer(sql);
SQLParser parser = new SQLParser(lexer.getAllTokens());
ASTNode ast = parser.parse();

// 综合分析
ComprehensiveAnalysisResult result = analyzer.analyze(ast);
System.out.println(result.getFormattedResult());

// 获取执行计划的不同格式
LogicalPlan plan = result.getExecutionPlan();
if (plan != null) {
    System.out.println(PlanFormatter.format(plan, OutputFormat.TREE));
    System.out.println(PlanFormatter.format(plan, OutputFormat.JSON));
    System.out.println(PlanFormatter.format(plan, OutputFormat.S_EXPR));
}
```

### 错误处理示例
```java
if (result.hasErrors()) {
    List<String> errors = result.getAllErrors();
    for (String error : errors) {
        System.err.println("错误: " + error);
    }
}
```

## 测试结果

系统成功处理了以下测试用例：

1. **正常SELECT**: ✅ 生成完整执行计划
2. **表不存在**: ✅ 正确检测并报错
3. **列不存在**: ✅ 正确检测并报错  
4. **CREATE TABLE**: ✅ 动态更新目录
5. **INSERT**: ✅ 类型检查和执行计划生成
6. **DELETE**: ✅ 条件分析和执行计划生成

## 四元式中间代码示例

```
SELECT id, name FROM users WHERE age > 18:
1: [SCAN, users, _, t1]
2: [>, age, 18, t2]  
3: [FILTER, t1, t2, t3]
4: [PROJECT, t3, id,name, t4]

CREATE TABLE orders (id INT, total FLOAT):
1: [CREATE_TABLE, orders, id:INT,total:FLOAT, _]

INSERT INTO users (id, name) VALUES (1, 'John'):
1: [INSERT, users, id,name, 1,'John']

DELETE FROM users WHERE id = 1:
1: [=, id, 1, t1]
2: [DELETE, users, t1, _]
```

## 架构优势

1. **模块化设计**: 语义分析和执行计划生成分离
2. **错误友好**: 详细的错误信息和位置定位
3. **多格式输出**: 支持多种执行计划表示格式
4. **类型安全**: 完善的类型检查和兼容性验证
5. **扩展性**: 易于添加新的SQL语句类型和检查规则

## 下一步开发方向

1. **高级语义特性**: 约束检查、外键验证
2. **查询优化**: 基于代价的优化器
3. **复杂查询**: JOIN操作、子查询支持
4. **执行引擎**: 将执行计划转换为实际的数据操作
5. **存储引擎**: 与底层存储系统集成

---

这个完整的语义分析器和执行计划生成器为SQL编译器提供了坚实的基础，满足了语义验证和代码生成的核心需求。
