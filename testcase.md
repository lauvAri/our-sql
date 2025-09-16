# OurSql Test Case
## 1. CREATE TABLE
ColumnTypes:`INT` `VARCHAR` `BOOLEAN` `FLOAT` `TIMESTAMP`

### 1.1 Correct Input
INPUT:

    CREATE TABLE student(id INT, name VARCHAR(255), age INT);

OUTPUT:

    Table created successfully.

### 1.2 Table exist
INPUT:

    CREATE TABLE student(id INT, name VARCHAR(255), age INT);

OUTPUT:

    1. 表 'student' 已存在

### 1.3 Invalid column type
INPUT:

    CREATE TABLE student(id INT, name VARCHAR(255), age BIGINT);

OUTPUT:

    语法错误: 没有为 DataType 和 IDENTIFIER:BIGINT 找到产生式


## 2. INSERT
### 2.1 Correct Input 
INPUT:

    INSERT INTO student(id,name,age) VALUES (1,'Alice',15);
    INSERT INTO student(id,name,age) VALUES (2,'Tom',19);
    INSERT INTO student(id,name,age) VALUES (3,'Jack',22);

OUTPUT:

    操作类型: INSERT
    插入表: student
    插入值: [[2, 'Tom', 19]]
    Insert successfully.

### 2.2 Table does not exist
INPUT:

    INSERT INTO user(id,name,age) VALUES (4,'Bob',19);

OUTPUT:

    Error: Execution complete.

### 2.3 Column value does not match column definition
INPUT:

    INSERT INTO student(id,name,age) VALUES ('4','Bob','19');

OUTPUT:

    错误信息:
    1. 列 'id' 的值类型不匹配，期望 INT，实际 VARCHAR
    2. 列 'age' 的值类型不匹配，期望 INT，实际 VARCHAR

## 3. SELECT
### 3.1 Basic Select
INPUT:

    SELECT id,name FROM student;

OUTPUT:

        id                  name                
    ------------------------------------------------------------
    1                   'Alice'             
    2                   'Tom'               
    3                   'Jack'              

    (3 rows)

### 3.2 Select with where
INPUT:

    SELECT * FROM student WHERE id = 1;

OUTPUT:

    id                  name                age                 
    ------------------------------------------------------------
    1                   'Alice'             15                  

    (1 rows)

### 3.3 Select with order By
INPUT:

    SELECT name,age FROM student ORDER BY age desc;

OUTPUT:

    name                age                 
    ------------------------------------------------------------
    'Jack'              22                  
    'Tom'               19                  
    'Alice'             15                  

    (3 rows)

#### 3.4 Select with limit
INPUT:

    SELECT name FROM student WHERE age > 12 LIMIT 2;

OUTPUT:

    name                
    ------------------------------------------------------------
    'Alice'             
    'Tom'
    
    (2 rows)

#### 3.5 Select undefined column
INPUT:

    SELECT class FROM student WHERE id = 1;

OUTPUT:

    错误信息:
    1. 列 'class' 在表 'student' 中不存在

## 4. UPDATE
### 4.1 Correct update
INPUT:

    UPDATE student SET age = 24 WHERE id = 1;

OUTPUT:

    操作类型: UPDATE
    更新表: student
    更新值: {age=24}
    更新条件: (id = '1')
    1

### 4.2 Update undefind table
INPUT:

    UPDATE student2 SET age = 20 WHERE id = 1;

OUTPUT:

    1. 表 'student2' 不存在

### 4.3 Update undefined column
INPUT:

    UPDATE student SET sage = 24 WHERE id = 1;

OUTPUT:

    错误信息:
    1. 表 'student' 中不存在列 'sage'

## 5. DELETE
### 5.1 Correct delete
INPUT:

    DELETE FROM student WHERE id = 1;

OUTPUT:

    Delete successfully.

### 5.2 delete undefined table
INPUT:

    DELETE FROM user WHERE id = 4;

OUTPUT:

    1. 表 'user' 不存在