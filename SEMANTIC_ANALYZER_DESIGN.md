# 语义分析器设计与实现

## 概述

成功设计并实现了SQL编译器的语义分析器，能够进行语义正确性检查并生成四元式中间代码。

## 核心组件

### 1. 语义分析器 (SemanticAnalyzer.java)
- **功能**：负责检查语义正确性并生成四元式中间代码
- **主要方法**：
  - `analyze(ASTNode ast)`: 分析AST节点的入口方法
  - `analyzeSelect()`: 分析SELECT语句
  - `analyzeCreateTable()`: 分析CREATE TABLE语句
  - `analyzeInsert()`: 分析INSERT语句
  - `analyzeDelete()`: 分析DELETE语句
  - `analyzeExpression()`: 分析表达式并生成四元式

### 2. 数据库目录 (Catalog.java)
- **功能**：管理表和列的元数据
- **特性**：
  - 预置系统表：users、product
  - 支持动态创建新表
  - 提供表/列存在性检查
  - 维护表结构信息

### 3. 表结构定义 (TableSchema.java)
- **功能**：定义表的结构信息
- **包含**：表名、列列表、列映射（快速查找）
- **方法**：列存在性检查、获取列信息、记录验证

### 4. 列结构定义 (ColumnSchema.java)
- **功能**：定义列的详细信息
- **属性**：列名、数据类型、是否可空、默认值、主键标识
- **特性**：类型兼容性检查、值验证

### 5. 四元式表示 (Quadruple.java)
- **格式**：[操作符, 操作数1, 操作数2, 结果]
- **支持操作**：SCAN、FILTER、PROJECT、CREATE_TABLE、INSERT、DELETE、比较操作符

### 6. 分析结果 (AnalysisResult.java)
- **内容**：四元式序列、错误信息、分析状态
- **功能**：格式化输出分析结果

### 7. AST字段访问器 (ASTFieldAccessor.java)
- **目的**：解决包访问权限问题
- **功能**：提供对AST节点字段的安全访问

## 语义检查功能

### 1. 表存在性检查
- 验证SELECT、INSERT、DELETE语句中引用的表是否存在
- 对不存在的表报告错误

### 2. 列存在性检查
- 验证SELECT列表中的列是否存在
- 验证INSERT语句中的列是否存在
- 验证WHERE条件中的列是否存在

### 3. 类型一致性验证
- 检查INSERT语句中值的类型是否与列类型兼容
- 检查WHERE条件中操作数的类型兼容性
- 支持数值类型间的兼容性（INT、FLOAT、DOUBLE）

### 4. 表创建验证
- 检查表名是否已存在
- 验证列名是否重复
- 验证数据类型是否有效

### 5. 类型推断
- 自动推断常量类型：
  - 整数：`INT`
  - 浮点数：`FLOAT`
  - 字符串：`VARCHAR`
  - 布尔值：`BOOLEAN`

## 四元式生成

### 1. SELECT语句四元式
```
[SCAN, 表名, _, 临时变量]           // 扫描表
[操作符, 左操作数, 右操作数, 临时变量]   // WHERE条件
[FILTER, 表临时变量, 条件临时变量, 临时变量] // 过滤
[PROJECT, 过滤临时变量, 列列表, 临时变量]   // 投影
```

### 2. CREATE TABLE语句四元式
```
[CREATE_TABLE, 表名, 列定义字符串, _]
```

### 3. INSERT语句四元式
```
[INSERT, 表名, 列列表, 值列表]
```

### 4. DELETE语句四元式
```
[操作符, 左操作数, 右操作数, 临时变量]   // WHERE条件（如果有）
[DELETE, 表名, 条件临时变量, _]
```

## 测试结果

### 成功案例
1. **正常SELECT**：`SELECT id, name FROM users WHERE age > 18;`
   - 生成：SCAN → 比较 → FILTER → PROJECT
   
2. **CREATE TABLE**：`CREATE TABLE orders (id INT, customer_id INT, total FLOAT);`
   - 成功创建表并添加到目录
   
3. **INSERT**：`INSERT INTO users (id, name, age) VALUES (1, 'John', 25);`
   - 类型检查通过，生成INSERT四元式
   
4. **DELETE**：`DELETE FROM users WHERE id = 1;`
   - 生成条件检查和DELETE四元式

### 错误检测案例
1. **不存在的表**：`SELECT * FROM nonexistent;`
   - 错误：表 'nonexistent' 不存在
   
2. **不存在的列**：`SELECT id, nonexistent_column FROM users;`
   - 错误：列 'nonexistent_column' 在表 'users' 中不存在

## 架构特点

### 1. 模块化设计
- 清晰的职责分离
- 各组件独立可测试
- 易于扩展和维护

### 2. 错误处理
- 详细的错误信息
- 错误位置报告
- 优雅的错误恢复

### 3. 扩展性
- 支持新的SQL语句类型
- 支持新的数据类型
- 支持更复杂的语义检查

### 4. 性能考虑
- 使用HashMap进行快速列查找
- 临时变量的有效管理
- 错误检查的短路处理

## 与语法分析器的集成

1. **AST接收**：接收语法分析器生成的AST
2. **字段访问**：通过ASTFieldAccessor安全访问AST节点字段
3. **错误协调**：语法错误和语义错误的统一处理
4. **类型推断**：基于语法分析的Token信息进行类型推断

## 未来扩展方向

1. **更多SQL特性**：
   - JOIN操作语义检查
   - 子查询支持
   - 聚合函数验证

2. **高级语义检查**：
   - 主键/外键约束
   - 唯一性约束
   - CHECK约束验证

3. **优化生成**：
   - 查询优化提示
   - 索引使用建议
   - 执行计划优化

4. **类型系统增强**：
   - 用户定义类型
   - 复合数据类型
   - 类型转换规则

语义分析器为SQL编译器提供了强大的语义验证能力，确保只有语义正确的SQL语句才能进入后续的执行阶段，同时生成的四元式为查询执行提供了标准化的中间表示。
