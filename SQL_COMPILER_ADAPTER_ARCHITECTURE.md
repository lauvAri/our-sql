# SQL编译器适配器架构设计文档

## 概述

本文档描述了SQL编译器模块与executor模块的集成架构，采用适配器模式实现了语义分析器与现有数据库系统的无缝对接。

## 架构设计

### 1. 适配器模式实现

#### 核心组件
- **CatalogInterface**: 为SQL编译器定义的目录服务接口
- **CatalogAdapter**: 适配器实现，将executor模块的CatalogManager适配为CatalogInterface
- **TableMetadata**: 轻量级表元数据传输对象
- **ColumnMetadata**: 轻量级列元数据传输对象

#### 架构优势
- **解耦**: SQL编译器模块与executor模块通过接口解耦
- **复用**: 充分利用executor模块现有的目录管理功能
- **扩展**: 易于扩展和维护，符合开放封闭原则

### 2. 语义分析器架构

#### 增强语义分析器 (EnhancedSemanticAnalyzer)
- 集成语义分析和执行计划生成
- 提供完整的SQL分析解决方案
- 支持结构化错误报告

#### 核心功能模块
1. **SemanticAnalyzer**: 语义正确性检查
2. **PlanGenerator**: 执行计划生成器
3. **PlanFormatter**: 多格式执行计划输出

### 3. 执行计划生成

#### LogicalPlan子类对象生成
根据SQL操作类型创建对应的LogicalPlan子类对象：
- **SelectPlan**: SELECT查询计划
- **CreateTablePlan**: 创建表计划
- **InsertPlan**: 插入数据计划  
- **DeletePlan**: 删除数据计划

#### 输出格式支持
- **树形结构**: 直观的层次化显示
- **JSON格式**: 结构化数据交换格式
- **S表达式**: 函数式表示格式

### 4. 错误处理系统

#### 结构化错误格式
```
[错误类型，位置，原因说明]
```

#### 错误类型枚举
- TABLE_NOT_FOUND: 表不存在
- COLUMN_NOT_FOUND: 列不存在
- TYPE_MISMATCH: 类型不匹配
- COLUMN_COUNT_MISMATCH: 列数不匹配
- DUPLICATE_COLUMN: 重复列名

## 主要文件清单

### 接口与适配器
- `semantic/CatalogInterface.java` - 目录服务接口定义
- `semantic/CatalogAdapter.java` - CatalogManager适配器实现
- `semantic/TableMetadata.java` - 表元数据传输对象
- `semantic/ColumnMetadata.java` - 列元数据传输对象

### 语义分析模块
- `semantic/EnhancedSemanticAnalyzer.java` - 增强语义分析器
- `semantic/SemanticAnalyzer.java` - 基础语义分析器
- `semantic/PlanGenerator.java` - 执行计划生成器
- `semantic/PlanFormatter.java` - 执行计划格式化器

### 错误处理
- `semantic/SemanticError.java` - 语义错误类
- `semantic/AnalysisResult.java` - 分析结果容器
- `semantic/PlanGenerationResult.java` - 计划生成结果容器

### 使用示例
- `semantic/SQLCompilerDemo.java` - 使用示例和演示
- `parser/Main.java` - 主程序入口（已更新为使用适配器）

## 使用方式

### 1. 初始化SQL编译器
```java
// 创建executor模块组件
StorageEngine storageEngine = ...;  // 实际的存储引擎
CatalogManager catalogManager = new CatalogManager(storageEngine);

// 创建适配器
CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);

// 创建增强语义分析器
EnhancedSemanticAnalyzer analyzer = new EnhancedSemanticAnalyzer(catalogAdapter);
```

### 2. 分析SQL语句
```java
// 解析SQL
ASTNode ast = SQLParser.parse(sql);

// 完整分析
EnhancedSemanticAnalyzer.ComprehensiveAnalysisResult result = analyzer.analyze(ast);

// 检查结果
if (result.isSuccess()) {
    LogicalPlan plan = result.getPlan();
    System.out.println("执行计划类型: " + plan.getClass().getSimpleName());
} else {
    result.getErrors().forEach(System.out::println);
}
```

### 3. 获取不同格式的执行计划
```java
PlanGenerationResult planResult = analyzer.generatePlan(ast);
if (planResult.isSuccess()) {
    LogicalPlan plan = planResult.getPlan();
    
    // 树形格式
    String treeFormat = PlanFormatter.format(plan, PlanFormatter.OutputFormat.TREE);
    
    // JSON格式
    String jsonFormat = PlanFormatter.format(plan, PlanFormatter.OutputFormat.JSON);
    
    // S表达式格式
    String sExprFormat = PlanFormatter.format(plan, PlanFormatter.OutputFormat.S_EXPR);
}
```

## 验证功能

### LogicalPlan对象类型验证
```java
// 验证生成的计划对象类型
String validation = PlanGenerator.validatePlanType(plan);
System.out.println(validation);
```

### 支持的SQL语句类型
- SELECT查询 → SelectPlan对象
- CREATE TABLE语句 → CreateTablePlan对象  
- INSERT语句 → InsertPlan对象
- DELETE语句 → DeletePlan对象

## 扩展性

### 新增SQL语句类型
1. 在PlanGenerator中添加新的生成方法
2. 创建对应的LogicalPlan子类
3. 更新SemanticAnalyzer的语义检查逻辑

### 新增输出格式
1. 在PlanFormatter.OutputFormat枚举中添加新格式
2. 在PlanFormatter.format方法中实现格式化逻辑

### 增强错误处理
1. 在SemanticError.ErrorType中添加新错误类型
2. 在相应分析器中添加错误检测逻辑

## 总结

本架构成功实现了：
- ✅ SQL编译器与executor模块的无缝集成
- ✅ 根据SQL操作类型创建对应LogicalPlan子类对象
- ✅ 结构化错误报告格式 [错误类型，位置，原因说明]
- ✅ 多格式执行计划输出支持
- ✅ 完整的语义分析和执行计划生成流程
- ✅ 高度可扩展的模块化设计

通过适配器模式，我们既保持了模块间的低耦合，又充分利用了现有代码，实现了高效的SQL编译器架构。
