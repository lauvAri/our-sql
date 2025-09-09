package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 四元式类
class Quadruple {
    public int index;
    public String op;
    public String arg1;
    public String arg2;
    public String result;

    public Quadruple(int index, String op, String arg1, String arg2, String result) {
        this.index = index;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    @Override
    public String toString() {
        return index + " (" + op + ", " + arg1 + ", " + arg2 + ", " + result + ")";
    }
}

// 符号表项
class SymbolTableEntry {
    public String name;
    public String type;
    public String value;

    public SymbolTableEntry(String name, String type) {
        this.name = name;
        this.type = type;
    }
}

// 符号表
class SymbolTable {
    private Map<String, SymbolTableEntry> table = new HashMap<>();
    private SymbolTable parent;

    public SymbolTable() {
        this.parent = null;
    }

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public boolean addSymbol(String name, String type) {
        if (table.containsKey(name)) {
            return false;
        }
        table.put(name, new SymbolTableEntry(name, type));
        return true;
    }

    public SymbolTableEntry lookup(String name) {
        SymbolTableEntry entry = table.get(name);
        if (entry != null) {
            return entry;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }
}

// 语义分析器
public class SemanticAnalyzer {
    private List<Quadruple> quadruples = new ArrayList<>();
    private SymbolTable currentSymbolTable = new SymbolTable();
    private int tempVarCount = 0;
    private int quadIndex = 0;

    public SemanticAnalyzer() {
        // 初始化内置类型和函数
        initializeBuiltins();
    }

    private void initializeBuiltins() {
        // 可以在这里添加内置函数和类型

        // 预先添加一些表和列信息
        currentSymbolTable.addSymbol("Students", "TABLE");
        currentSymbolTable.addSymbol("name", "VARCHAR");
        currentSymbolTable.addSymbol("age", "INT");
        currentSymbolTable.addSymbol("id", "INT");

        // 可以在这里添加其他内置表和列
        currentSymbolTable.addSymbol("users", "TABLE");
        currentSymbolTable.addSymbol("username", "VARCHAR");
        currentSymbolTable.addSymbol("email", "VARCHAR");
        currentSymbolTable.addSymbol("created_at", "DATE");

        currentSymbolTable.addSymbol("products", "TABLE");
        currentSymbolTable.addSymbol("product_id", "INT");
        currentSymbolTable.addSymbol("product_name", "VARCHAR");
        currentSymbolTable.addSymbol("stock", "INT");

        currentSymbolTable.addSymbol("logs", "TABLE");
        currentSymbolTable.addSymbol("log_id", "INT");
        currentSymbolTable.addSymbol("created_at", "DATE");
        currentSymbolTable.addSymbol("message", "VARCHAR");
    }

    // 生成临时变量
    public String newTemp() {
        return "T" + (tempVarCount++);
    }

    // 添加四元式
    public void addQuadruple(String op, String arg1, String arg2, String result) {
        quadruples.add(new Quadruple(quadIndex++, op, arg1, arg2, result));
    }

    // 处理SELECT语句的语义动作
    public void processSelectStatement(SelectNode selectNode) {
        // 检查表是否存在
        SymbolTableEntry tableEntry = currentSymbolTable.lookup(selectNode.tableName);
        if (tableEntry == null) {
            System.err.println("语义错误: 表 " + selectNode.tableName + " 不存在");
            return;
        }

        // 处理选择的列
        List<String> columnTemps = new ArrayList<>();
        for (String column : selectNode.columns) {
            // 检查列是否存在
            SymbolTableEntry columnEntry = currentSymbolTable.lookup(column);
            if (columnEntry == null) {
                System.err.println("语义错误: 列 " + column + " 不存在");
                return;
            }

            String tempVar = newTemp();
            addQuadruple("SELECT", column, "-", tempVar);
            columnTemps.add(tempVar);
        }

        // 处理FROM子句
        String fromTemp = newTemp();
        addQuadruple("FROM", selectNode.tableName, "-", fromTemp);

        // 处理WHERE子句
        String whereTemp = null;
        if (selectNode.whereClause != null) {
            whereTemp = processExpression(selectNode.whereClause);
        }

        // 生成RESULT四元式
        String columnsStr = String.join(",", columnTemps);
        addQuadruple("RESULT", columnsStr, fromTemp, whereTemp != null ? whereTemp : "-");
    }

    // 处理表达式
    private String processExpression(ExpressionNode expr) {
        String left = processOperand(expr.left);
        String right = processOperand(expr.right);
        String temp = newTemp();
        addQuadruple(expr.operator, left, right, temp);
        return temp;
    }

    // 处理操作数
    private String processOperand(Object operand) {
        if (operand instanceof String) {
            return (String) operand;
        } else if (operand instanceof ExpressionNode) {
            return processExpression((ExpressionNode) operand);
        } else {
            return operand.toString();
        }
    }

    // 处理CREATE TABLE语句的语义动作
    public void processCreateTable(CreateTableNode createNode) {
        // 检查表是否已存在
        if (currentSymbolTable.contains(createNode.tableName)) {
            System.err.println("语义错误: 表 " + createNode.tableName + " 已存在");
            return;
        }

        // 创建表符号
        currentSymbolTable.addSymbol(createNode.tableName, "TABLE");

        // 处理列定义
        for (ColumnDefinition column : createNode.columns) {
            // 检查列是否已存在
            if (currentSymbolTable.contains(column.name)) {
                System.err.println("语义错误: 列 " + column.name + " 已存在");
                return;
            }

            currentSymbolTable.addSymbol(column.name, column.dataType);
            addQuadruple("CREATE_COLUMN", column.name, column.dataType, "-");
        }

        addQuadruple("CREATE_TABLE", createNode.tableName, "-", "-");
    }

    // 处理INSERT语句的语义动作
    public void processInsertStatement(InsertNode insertNode) {
        // 检查表是否存在
        SymbolTableEntry tableEntry = currentSymbolTable.lookup(insertNode.tableName);
        if (tableEntry == null) {
            System.err.println("语义错误: 表 " + insertNode.tableName + " 不存在");
            return;
        }

        // 检查列和值的数量是否匹配
        if (insertNode.columns.size() != insertNode.values.size()) {
            System.err.println("语义错误: 列和值的数量不匹配");
            return;
        }

        // 处理每一列
        for (int i = 0; i < insertNode.columns.size(); i++) {
            String column = insertNode.columns.get(i);
            Object value = insertNode.values.get(i);

            // 检查列是否存在
            SymbolTableEntry columnEntry = currentSymbolTable.lookup(column);
            if (columnEntry == null) {
                System.err.println("语义错误: 列 " + column + " 不存在");
                return;
            }

            addQuadruple("INSERT", column, value.toString(), "-");
        }

        addQuadruple("INTO", insertNode.tableName, "-", "-");
    }

    // 处理UPDATE语句的语义动作
    public void processUpdateStatement(UpdateNode updateNode) {
        // 检查表是否存在
        SymbolTableEntry tableEntry = currentSymbolTable.lookup(updateNode.tableName);
        if (tableEntry == null) {
            System.err.println("语义错误: 表 " + updateNode.tableName + " 不存在");
            return;
        }

        // 处理SET子句
        for (SetClause setClause : updateNode.setClauses) {
            // 检查列是否存在
            SymbolTableEntry columnEntry = currentSymbolTable.lookup(setClause.column);
            if (columnEntry == null) {
                System.err.println("语义错误: 列 " + setClause.column + " 不存在");
            }

            // 处理表达式
            String valueTemp = processExpression(setClause.value);
            addQuadruple("SET", setClause.column, valueTemp, "-");
        }

        // 处理WHERE子句
        String whereTemp = null;
        if (updateNode.whereClause != null) {
            whereTemp = processExpression(updateNode.whereClause);
        }

        addQuadruple("UPDATE", updateNode.tableName, whereTemp != null ? whereTemp : "-", "-");
    }

    // 处理DELETE语句的语义动作
    public void processDeleteStatement(DeleteNode deleteNode) {
        // 检查表是否存在
        SymbolTableEntry tableEntry = currentSymbolTable.lookup(deleteNode.tableName);
        if (tableEntry == null) {
            System.err.println("语义错误: 表 " + deleteNode.tableName + " 不存在");
            return;
        }

        // 处理WHERE子句
        String whereTemp = null;
        if (deleteNode.whereClause != null) {
            whereTemp = processExpression(deleteNode.whereClause);
        }

        addQuadruple("DELETE", deleteNode.tableName, whereTemp != null ? whereTemp : "-", "-");
    }

    // 获取生成的四元式
    public List<Quadruple> getQuadruples() {
        return quadruples;
    }

    // 打印四元式
    public void printQuadruples() {
        for (Quadruple quad : quadruples) {
            System.out.println(quad);
        }
    }
}