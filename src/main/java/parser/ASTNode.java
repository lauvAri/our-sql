package parser;

import java.util.ArrayList;
import java.util.List;

// AST节点基类
abstract class ASTNode {
    protected String type;

    public String getType() {
        return type;
    }

    public abstract String toString();
}

// SELECT语句节点
class SelectNode extends ASTNode {
    public List<String> columns;
    public String tableName;
    public ExpressionNode whereClause;

    public SelectNode() {
        this.type = "SELECT";
        this.columns = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("SELECT %s FROM %s WHERE %s",
                columns, tableName, whereClause != null ? whereClause.toString() : "NULL");
    }
}

// UPDATE语句节点
class UpdateNode extends ASTNode {
    public String tableName;
    public List<SetClause> setClauses;
    public ExpressionNode whereClause;

    public UpdateNode() {
        this.type = "UPDATE";
        this.setClauses = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("UPDATE %s SET %s WHERE %s",
                tableName, setClauses, whereClause != null ? whereClause.toString() : "NULL");
    }
}

// INSERT语句节点
class InsertNode extends ASTNode {
    public String tableName;
    public List<String> columns;
    public List<Object> values;

    public InsertNode() {
        this.type = "INSERT";
        this.columns = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns, values);
    }
}

// DELETE语句节点
class DeleteNode extends ASTNode {
    public String tableName;
    public ExpressionNode whereClause;

    public DeleteNode() {
        this.type = "DELETE";
    }

    @Override
    public String toString() {
        return String.format("DELETE FROM %s WHERE %s",
                tableName, whereClause != null ? whereClause.toString() : "NULL");
    }
}

// CREATE TABLE语句节点
class CreateTableNode extends ASTNode {
    public String tableName;
    public List<ColumnDefinition> columns;

    public CreateTableNode() {
        this.type = "CREATE_TABLE";
        this.columns = new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("CREATE TABLE %s (%s)", tableName, columns);
    }
}

// SET子句（用于UPDATE语句）
class SetClause {
    public String column;
    public ExpressionNode value;  // 修改为ExpressionNode类型

    public SetClause(String column, ExpressionNode value) {
        this.column = column;
        this.value = value;
    }

    @Override
    public String toString() {
        return column + " = " + value;
    }
}

// 列定义
class ColumnDefinition {
    public String name;
    public String dataType;

    public ColumnDefinition(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return name + " " + dataType;
    }
}

// 表达式节点
class ExpressionNode {
    public Object left;
    public String operator;
    public Object right;

    public ExpressionNode(Object left, String operator, Object right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
}