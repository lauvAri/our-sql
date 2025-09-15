# SQL子集语言规范

## 1. 词法规则 (Lexical Rules)

### 1.1 关键字 (Keywords)
```
SELECT, FROM, WHERE, CREATE, TABLE, INSERT, INTO, VALUES, DELETE, 
UPDATE, SET, AND, OR, NOT, NULL, INT, VARCHAR, CHAR, DATE, FLOAT, 
DOUBLE, BOOLEAN, PRIMARY, KEY, DROP, ALTER, CHECK, IN, JOIN, ON, 
TRUE, FALSE
```

### 1.2 标识符 (Identifiers)
- **规则**: 以字母或下划线开头，后跟字母、数字或下划线的序列
- **正则表达式**: `[a-zA-Z_][a-zA-Z0-9_]*`
- **示例**: `user_id`, `product_name`, `table1`

### 1.3 常量 (Constants)

#### 1.3.1 数字常量
- **整数**: `[0-9]+`
- **浮点数**: `[0-9]+\.[0-9]+`
- **示例**: `123`, `45.67`, `0`, `999.99`

#### 1.3.2 字符串常量
- **规则**: 单引号包围的字符序列，支持转义字符
- **正则表达式**: `'([^'\\]|\\.)*'`
- **示例**: `'Hello'`, `'John\'s book'`, `'2024-01-15'`

#### 1.3.3 布尔常量
- **值**: `TRUE`, `FALSE`

### 1.4 运算符 (Operators)
```
=, >, <, >=, <=, <>, !=, +, -, *, /, %
```

### 1.5 分隔符 (Delimiters)
```
, ; ( ) { }
```

### 1.6 特殊符号
- **通配符**: `*` (用于 SELECT *)

### 1.7 注释 (Comments)
- **单行注释**: `-- 注释内容`
- **多行注释**: `/* 注释内容 */`

## 2. 语法规则 (Grammar Rules)

### 2.1 BNF文法

```bnf
<程序> ::= <语句> ;

<语句> ::= <查询语句> | <创建表语句> | <插入语句> | <删除语句>

<查询语句> ::= SELECT <选择列表> FROM <表名> <WHERE子句>

<选择列表> ::= <列名> <选择列表尾部> | *

<选择列表尾部> ::= , <列名> <选择列表尾部> | ε

<WHERE子句> ::= WHERE <条件> | ε

<条件> ::= <列名> <运算符> <值>

<运算符> ::= = | > | < | >= | <= | <>

<值> ::= <标识符> | <常量> | <布尔值>

<布尔值> ::= TRUE | FALSE

<创建表语句> ::= CREATE TABLE <表名> ( <列定义列表> )

<列定义列表> ::= <列定义> <列定义列表尾部>

<列定义列表尾部> ::= , <列定义> <列定义列表尾部> | ε

<列定义> ::= <列名> <数据类型>

<数据类型> ::= INT | VARCHAR | CHAR | DATE | FLOAT | DOUBLE | BOOLEAN

<插入语句> ::= INSERT INTO <表名> ( <列列表> ) VALUES ( <值列表> )

<列列表> ::= <列名> <列列表尾部>

<列列表尾部> ::= , <列名> <列列表尾部> | ε

<值列表> ::= <值> <值列表尾部>

<值列表尾部> ::= , <值> <值列表尾部> | ε

<删除语句> ::= DELETE FROM <表名> <WHERE子句>

<表名> ::= <标识符>

<列名> ::= <标识符>
```

### 2.2 产生式规则 (Production Rules)

#### 2.2.1 程序结构
1. `Prog → Stmt ;`

#### 2.2.2 语句类型
2. `Stmt → Query`
3. `Stmt → CreateTable`
4. `Stmt → Insert`
5. `Stmt → Delete`

#### 2.2.3 SELECT语句
6. `Query → SELECT SelList FROM Tbl WhereClause`
7. `SelList → ID SelListTail`
8. `SelList → * SelListTail`
9. `SelListTail → , ID SelListTail`
10. `SelListTail → ε`

#### 2.2.4 WHERE子句
11. `WhereClause → WHERE Condition`
12. `WhereClause → ε`
13. `Condition → ID Operator Value`

#### 2.2.5 运算符和值
14. `Operator → = | > | < | >= | <= | <>`
15. `Value → ID | CONSTANT | TRUE | FALSE`

#### 2.2.6 CREATE TABLE语句
16. `CreateTable → CREATE TABLE ID ( ColDefList )`
17. `ColDefList → ColDef ColDefListTail`
18. `ColDefListTail → , ColDef ColDefListTail`
19. `ColDefListTail → ε`
20. `ColDef → ID DataType`
21. `DataType → INT | VARCHAR | CHAR | DATE | FLOAT | DOUBLE | BOOLEAN`

#### 2.2.7 INSERT语句
22. `Insert → INSERT INTO ID ( ColList ) VALUES ( ValList )`
23. `ColList → ID ColListTail`
24. `ColListTail → , ID ColListTail`
25. `ColListTail → ε`
26. `ValList → Value ValListTail`
27. `ValListTail → , Value ValListTail`
28. `ValListTail → ε`

#### 2.2.8 DELETE语句
29. `Delete → DELETE FROM Tbl WhereClause`
30. `Tbl → ID`

## 3. 语义规则 (Semantic Rules)

### 3.1 标识符规则
- 表名和列名必须是有效的标识符
- 标识符大小写不敏感（关键字）
- 用户定义的标识符保持大小写

### 3.2 数据类型规则
- `INT`: 整数类型
- `FLOAT`/`DOUBLE`: 浮点数类型
- `VARCHAR`/`CHAR`: 字符串类型
- `DATE`: 日期类型，格式为 'YYYY-MM-DD'
- `BOOLEAN`: 布尔类型，值为 TRUE 或 FALSE

### 3.3 运算符语义
- `=`: 等于
- `>`: 大于
- `<`: 小于
- `>=`: 大于等于
- `<=`: 小于等于
- `<>`: 不等于

### 3.4 语句语义

#### 3.4.1 SELECT语句
- 从指定表中查询数据
- `*` 表示选择所有列
- WHERE子句是可选的，用于过滤条件

#### 3.4.2 CREATE TABLE语句
- 创建新表及其结构
- 每列必须指定名称和数据类型

#### 3.4.3 INSERT语句
- 向表中插入数据
- 列数和值数必须匹配
- 值的类型必须与列的数据类型兼容

#### 3.4.4 DELETE语句
- 从表中删除数据
- WHERE子句用于指定删除条件
- 如果没有WHERE子句，删除所有行

## 4. 错误处理

### 4.1 词法错误
- 非法字符
- 未闭合的字符串
- 格式错误的数字

### 4.2 语法错误
- 缺少关键字
- 语法结构不完整
- 意外的标记

### 4.3 语义错误
- 未定义的表或列
- 类型不匹配
- 约束违反

## 5. 抽象语法树 (AST) 结构

### 5.1 节点类型
- `SelectNode`: SELECT语句节点
- `CreateTableNode`: CREATE TABLE语句节点
- `InsertNode`: INSERT语句节点
- `DeleteNode`: DELETE语句节点
- `ExpressionNode`: 表达式节点
- `ColumnDefinition`: 列定义节点

### 5.2 AST属性
每个AST节点包含相应的语义信息，如表名、列名、条件表达式等。

## 6. 示例

### 6.1 SELECT语句示例
```sql
SELECT * FROM product;
SELECT id, name FROM users WHERE age > 18;
```

### 6.2 CREATE TABLE语句示例
```sql
CREATE TABLE product (
    id INT,
    name VARCHAR,
    price FLOAT,
    available BOOLEAN
);
```

### 6.3 INSERT语句示例
```sql
INSERT INTO users (id, name, age) VALUES (1, 'John', 25);
```

### 6.4 DELETE语句示例
```sql
DELETE FROM users WHERE id = 1;
```
