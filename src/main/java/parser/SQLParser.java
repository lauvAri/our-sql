package parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

public class SQLParser {
    private List<Token> tokens;
    private int currentTokenIndex;
    private Stack<String> parseStack;
    private StringBuilder output;
    private int step;

    // AST构建相关的变量
    private ASTNode ast;
    private Stack<Object> astStack; // 用于构建AST的栈

    // 非终结符
    private static final String PROG = "Prog";
    private static final String STMT = "Stmt";
    private static final String QUERY = "Query";
    private static final String CREATE_TABLE = "CreateTable";
    private static final String INSERT = "Insert";
    private static final String UPDATE = "Update";
    private static final String DELETE = "Delete";
    private static final String SELLIST = "SelList";
    private static final String SELLIST_TAIL = "SelListTail";
    private static final String TBL = "Tbl";
    private static final String WHERE_CLAUSE = "WhereClause";
    private static final String ORDER_CLAUSE = "OrderClause";
    private static final String ORDER_LIST = "OrderList";
    private static final String ORDER_LIST_TAIL = "OrderListTail";
    private static final String ORDER_ITEM = "OrderItem";
    private static final String ORDER_DIRECTION = "OrderDirection";
    private static final String LIMIT_CLAUSE = "LimitClause";
    private static final String LOGICAL_EXPRESSION = "LogicalExpression";
    private static final String LOGICAL_EXPRESSION_TAIL = "LogicalExpressionTail";
    private static final String LOGICAL_TERM = "LogicalTerm";
    private static final String LOGICAL_TERM_TAIL = "LogicalTermTail";
    private static final String LOGICAL_FACTOR = "LogicalFactor";
    private static final String CONDITION = "Condition";
    private static final String VALUE = "Value";
    private static final String OPERATOR = "Operator";
    private static final String COL_DEF_LIST = "ColDefList";
    private static final String COL_DEF_LIST_TAIL = "ColDefListTail";
    private static final String COL_DEF = "ColDef";
    private static final String DATA_TYPE = "DataType";
    private static final String COLUMN_CONSTRAINTS = "ColumnConstraints";
    private static final String COLUMN_CONSTRAINT = "ColumnConstraint";
    private static final String COL_LIST = "ColList";
    private static final String COL_LIST_TAIL = "ColListTail";
    private static final String SET_CLAUSE = "SetClause";
    private static final String SET_LIST = "SetList";
    private static final String SET_LIST_TAIL = "SetListTail";
    private static final String SET_ITEM = "SetItem";
    private static final String VAL_LIST = "ValList";
    private static final String VAL_LIST_TAIL = "ValListTail";

    public SQLParser(List<Token> tokens) {
        // 过滤掉注释Token
        this.tokens = new ArrayList<>();
        for (Token token : tokens) {
            if (token.getType() != Token.TokenType.COMMENT) {
                this.tokens.add(token);
            }
        }

        this.currentTokenIndex = 0;
        this.parseStack = new Stack<>();
        this.output = new StringBuilder();
        this.step = 1;
        this.astStack = new Stack<>();

        // 初始化分析栈
        parseStack.push("$"); // 结束符号
        parseStack.push(PROG); // 开始符号
    }
    
    // 输出语法分析四元式：[步骤，[语法栈]，（输入串），表达式]
    private void outputParseStep(String action) {
        String stackContent = parseStack.toString();
        String inputString = getRemainingInput();
        System.out.println("  步骤[" + step + "]: [" + step + ", " + stackContent + ", (" + inputString + "), " + action + "]");
        step++;
    }
    
    // 获取剩余输入串
    private String getRemainingInput() {
        StringBuilder remaining = new StringBuilder();
        for (int i = currentTokenIndex; i < tokens.size(); i++) {
            if (remaining.length() > 0) remaining.append(" ");
            remaining.append(tokens.get(i).getValue());
        }
        return remaining.toString();
    }

    // 获取当前Token
    private Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return null;
    }

    // 获取当前Token的类型字符串表示
    private String getCurrentTokenType() {
        Token token = getCurrentToken();
        if (token != null) {
            return token.getType().toString();
        }
        return "EOF";
    }

    // 获取当前Token的值
    private String getCurrentTokenValue() {
        Token token = getCurrentToken();
        if (token != null) {
            return token.getValue();
        }
        return "";
    }

    // 消费当前Token
    private void consumeToken() {
        if (currentTokenIndex < tokens.size()) {
            currentTokenIndex++;
        }
    }

    // 记录分析步骤
    private void recordStep(String action) {
        output.append(String.format("[%d, %s, (%s), %s]\n",
                step++,
                parseStack.toString(),
                getRemainingInputWithValues(),
                action));
    }

    // 获取剩余输入（带值）
    private String getRemainingInputWithValues() {
        StringBuilder sb = new StringBuilder();
        for (int i = currentTokenIndex; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String type = token.getType().toString();
            String value = token.getValue();

            if (type.equals("IDENTIFIER") || type.equals("CONSTANT")) {
                sb.append(type).append(":").append(value).append(" ");
            } else {
                sb.append(value).append(" ");
            }
        }
        sb.append("$");
        return sb.toString();
    }

    // 语法分析入口
    public ASTNode parse() {
        System.out.println("🎯 语法分析四元式输出格式: [步骤，[语法栈]，（输入串），表达式]");
        
        output.append("开始语法分析:\n");
        recordStep("开始");
        outputParseStep("初始化");

        while (!parseStack.isEmpty()) {
            String top = parseStack.peek();

            // 如果栈顶是终结符
            if (isTerminal(top)) {
                Token currentToken = getCurrentToken();
                if (currentToken != null) {
                    // 检查是否匹配
                    boolean matched = false;
                    String matchedValue = "";

                    if (top.equals("SELECT") && currentToken.getValue().equalsIgnoreCase("SELECT")) {
                        matched = true;
                        matchedValue = "SELECT";
                        outputParseStep("匹配终结符: " + matchedValue);
                        // 开始构建SELECT节点
                        astStack.push(new SelectNode());
                    } else if (top.equals("CREATE") && currentToken.getValue().equalsIgnoreCase("CREATE")) {
                        matched = true;
                        matchedValue = "CREATE";
                        outputParseStep("匹配终结符: " + matchedValue);
                        // 开始构建CREATE TABLE节点
                        astStack.push(new CreateTableNode());
                    } else if (top.equals("TABLE") && currentToken.getValue().equalsIgnoreCase("TABLE")) {
                        matched = true;
                        matchedValue = "TABLE";
                        outputParseStep("匹配终结符: " + matchedValue);
                    } else if (top.equals("INSERT") && currentToken.getValue().equalsIgnoreCase("INSERT")) {
                        matched = true;
                        matchedValue = "INSERT";
                        outputParseStep("匹配终结符: " + matchedValue);
                        // 开始构建INSERT节点
                        astStack.push(new InsertNode());
                    } else if (top.equals("INTO") && currentToken.getValue().equalsIgnoreCase("INTO")) {
                        matched = true;
                        matchedValue = "INTO";
                        outputParseStep("匹配终结符: " + matchedValue);
                    } else if (top.equals("VALUES") && currentToken.getValue().equalsIgnoreCase("VALUES")) {
                        matched = true;
                        matchedValue = "VALUES";
                        outputParseStep("匹配终结符: " + matchedValue);
                    } else if (top.equals("DELETE") && currentToken.getValue().equalsIgnoreCase("DELETE")) {
                        matched = true;
                        matchedValue = "DELETE";
                        outputParseStep("匹配终结符: " + matchedValue);
                        // 开始构建DELETE节点
                        astStack.push(new DeleteNode());
                    } else if (top.equals("UPDATE") && currentToken.getValue().equalsIgnoreCase("UPDATE")) {
                        matched = true;
                        matchedValue = "UPDATE";
                        outputParseStep("匹配终结符: " + matchedValue);
                        // 开始构建UPDATE节点
                        astStack.push(new UpdateNode());
                    } else if (top.equals("SET") && currentToken.getValue().equalsIgnoreCase("SET")) {
                        matched = true;
                        matchedValue = "SET";
                        astStack.push("SET");
                    } else if (top.equals("FROM") && currentToken.getValue().equalsIgnoreCase("FROM")) {
                        matched = true;
                        matchedValue = "FROM";
                        astStack.push("FROM");
                    } else if (top.equals("WHERE") && currentToken.getValue().equalsIgnoreCase("WHERE")) {
                        matched = true;
                        matchedValue = "WHERE";
                        astStack.push("WHERE");
                    } else if (top.equals("ORDER") && currentToken.getValue().equalsIgnoreCase("ORDER")) {
                        matched = true;
                        matchedValue = "ORDER";
                        astStack.push("ORDER");
                    } else if (top.equals("BY") && currentToken.getValue().equalsIgnoreCase("BY")) {
                        matched = true;
                        matchedValue = "BY";
                        astStack.push("BY");
                    } else if (top.equals("LIMIT") && currentToken.getValue().equalsIgnoreCase("LIMIT")) {
                        matched = true;
                        matchedValue = "LIMIT";
                        astStack.push("LIMIT");
                    } else if (top.equals("ASC") && currentToken.getValue().equalsIgnoreCase("ASC")) {
                        matched = true;
                        matchedValue = "ASC";
                        astStack.push("ASC");
                    } else if (top.equals("DESC") && currentToken.getValue().equalsIgnoreCase("DESC")) {
                        matched = true;
                        matchedValue = "DESC";
                        astStack.push("DESC");
                    } else if (top.equals("ID") && currentToken.getType() == Token.TokenType.IDENTIFIER) {
                        matched = true;
                        matchedValue = "ID:" + currentToken.getValue();
                        // 将标识符压入AST栈
                        astStack.push(currentToken.getValue());
                    } else if (top.equals("CONSTANT") && currentToken.getType() == Token.TokenType.CONSTANT) {
                        matched = true;
                        matchedValue = "CONSTANT:" + currentToken.getValue();
                        // 将常量压入AST栈
                        astStack.push(currentToken.getValue());
                    } else if (top.equals(";") && currentToken.getValue().equals(";")) {
                        matched = true;
                        matchedValue = ";";
                        // 语句结束，构建最终的AST节点
                        buildFinalAST();
                    } else if (top.equals(",") && currentToken.getValue().equals(",")) {
                        matched = true;
                        matchedValue = ",";
                    } else if (top.equals("(") && currentToken.getValue().equals("(")) {
                        matched = true;
                        matchedValue = "(";
                    } else if (top.equals(")") && currentToken.getValue().equals(")")) {
                        matched = true;
                        matchedValue = ")";
                    } else if (top.equals("=") && currentToken.getValue().equals("=")) {
                        matched = true;
                        matchedValue = "=";
                        astStack.push("=");
                        // 如果条件的三个部分都收集完了，构建ExpressionNode
                        buildConditionIfComplete();
                    } else if (top.equals(">") && currentToken.getValue().equals(">")) {
                        matched = true;
                        matchedValue = ">";
                        astStack.push(">");
                        // 如果条件的三个部分都收集完了，构建ExpressionNode
                        buildConditionIfComplete();
                    } else if (top.equals("<") && currentToken.getValue().equals("<")) {
                        matched = true;
                        matchedValue = "<";
                        astStack.push("<");
                        buildConditionIfComplete();
                    } else if (top.equals(">=") && currentToken.getValue().equals(">=")) {
                        matched = true;
                        matchedValue = ">=";
                        astStack.push(">=");
                        buildConditionIfComplete();
                    } else if (top.equals("<=") && currentToken.getValue().equals("<=")) {
                        matched = true;
                        matchedValue = "<=";
                        astStack.push("<=");
                        buildConditionIfComplete();
                    } else if (top.equals("<>") && currentToken.getValue().equals("<>")) {
                        matched = true;
                        matchedValue = "<>";
                        astStack.push("<>");
                        buildConditionIfComplete();
                    } else if (top.equals("INT") && currentToken.getValue().equalsIgnoreCase("INT")) {
                        matched = true;
                        matchedValue = "INT";
                        astStack.push("INT");
                    } else if (top.equals("VARCHAR") && currentToken.getValue().equalsIgnoreCase("VARCHAR")) {
                        matched = true;
                        matchedValue = "VARCHAR";
                        astStack.push("VARCHAR");
                    } else if (top.equals("CHAR") && currentToken.getValue().equalsIgnoreCase("CHAR")) {
                        matched = true;
                        matchedValue = "CHAR";
                        astStack.push("CHAR");
                    } else if (top.equals("DATE") && currentToken.getValue().equalsIgnoreCase("DATE")) {
                        matched = true;
                        matchedValue = "DATE";
                        astStack.push("DATE");
                    } else if (top.equals("FLOAT") && currentToken.getValue().equalsIgnoreCase("FLOAT")) {
                        matched = true;
                        matchedValue = "FLOAT";
                        astStack.push("FLOAT");
                    } else if (top.equals("DOUBLE") && currentToken.getValue().equalsIgnoreCase("DOUBLE")) {
                        matched = true;
                        matchedValue = "DOUBLE";
                        astStack.push("DOUBLE");
                    } else if (top.equals("BOOLEAN") && currentToken.getValue().equalsIgnoreCase("BOOLEAN")) {
                        matched = true;
                        matchedValue = "BOOLEAN";
                        astStack.push("BOOLEAN");
                    } else if (top.equals("PRIMARY") && currentToken.getValue().equalsIgnoreCase("PRIMARY")) {
                        matched = true;
                        matchedValue = "PRIMARY";
                        astStack.push("PRIMARY");
                    } else if (top.equals("KEY") && currentToken.getValue().equalsIgnoreCase("KEY")) {
                        matched = true;
                        matchedValue = "KEY";
                        astStack.push("KEY");
                    } else if (top.equals("TRUE") && currentToken.getValue().equalsIgnoreCase("TRUE")) {
                        matched = true;
                        matchedValue = "TRUE";
                        astStack.push("TRUE");
                    } else if (top.equals("FALSE") && currentToken.getValue().equalsIgnoreCase("FALSE")) {
                        matched = true;
                        matchedValue = "FALSE";
                        astStack.push("FALSE");
                    } else if (top.equals("AND") && currentToken.getValue().equalsIgnoreCase("AND")) {
                        matched = true;
                        matchedValue = "AND";
                        astStack.push("AND");
                    } else if (top.equals("OR") && currentToken.getValue().equalsIgnoreCase("OR")) {
                        matched = true;
                        matchedValue = "OR";
                        astStack.push("OR");
                    } else if (top.equals("NOT") && currentToken.getValue().equalsIgnoreCase("NOT")) {
                        matched = true;
                        matchedValue = "NOT";
                        astStack.push("NOT");
                    } else if (top.equals("IN") && currentToken.getValue().equalsIgnoreCase("IN")) {
                        matched = true;
                        matchedValue = "IN";
                        astStack.push("IN");
                    } else if (top.equals("IS") && currentToken.getValue().equalsIgnoreCase("IS")) {
                        matched = true;
                        matchedValue = "IS";
                        astStack.push("IS");
                    } else if (top.equals("NULL") && currentToken.getValue().equalsIgnoreCase("NULL")) {
                        matched = true;
                        matchedValue = "NULL";
                        astStack.push("NULL");
                    } else if (top.equals("*") && currentToken.getValue().equals("*")) {
                        matched = true;
                        matchedValue = "*";
                        astStack.push("*");
                    }

                    if (matched) {
                        // 匹配成功，弹出栈顶
                        parseStack.pop();
                        recordStep("匹配 " + matchedValue);
                        consumeToken();
                    } else {
                        // 错误处理
                        recordStep("错误: 期望 " + top + " 但找到 " + getCurrentTokenType() + ":" + getCurrentTokenValue());
                        outputParseStep("匹配失败: 期望 " + top + " 但找到 " + getCurrentTokenType() + ":" + getCurrentTokenValue());
                        return error("语法错误: 期望 " + top + " 但找到 " + getCurrentTokenType() + ":" + getCurrentTokenValue());
                    }
                } else {
                    // 输入结束但栈未空
                    recordStep("错误: 期望 " + top + " 但输入已结束");
                    outputParseStep("输入结束错误: 期望 " + top + " 但输入已结束");
                    return error("语法错误: 期望 " + top + " 但输入已结束");
                }
            }
            // 如果栈顶是非终结符
            else {
                // 根据预测分析表选择产生式
                String production = getProduction(top, getCurrentTokenType(), getCurrentTokenValue());
                if (production != null) {
                    parseStack.pop(); // 弹出非终结符

                    // 将产生式右部逆序压入栈中
                    String[] symbols = production.split(" ");
                    for (int i = symbols.length - 1; i >= 0; i--) {
                        if (!symbols[i].equals("ε")) { // 空产生式不压栈
                            parseStack.push(symbols[i]);
                        }
                    }

                    // 处理AST构建
                    handleASTConstruction(top, production);

                    recordStep("用(" + (step-1) + ") " + top + " → " + production);
                    outputParseStep("应用产生式: " + top + " → " + production);
                } else {
                    // 错误处理
                    recordStep("错误: 没有为 " + top + " 和 " + getCurrentTokenType() + ":" + getCurrentTokenValue() + " 找到产生式");
                    outputParseStep("语法错误: 没有为 " + top + " 和 " + getCurrentTokenType() + ":" + getCurrentTokenValue() + " 找到产生式");
                    return error("语法错误: 没有为 " + top + " 和 " + getCurrentTokenType() + ":" + getCurrentTokenValue() + " 找到产生式");
                }
            }

            // 检查是否到达输入结束
            if (getCurrentToken() == null && parseStack.peek().equals("$")) {
                recordStep("接受(Accept)");
                outputParseStep("语法分析成功 - 接受状态");
                break;
            } else if (getCurrentToken() == null && !parseStack.peek().equals("$")) {
                recordStep("错误: 输入已结束但栈未空");
                outputParseStep("语法错误: 输入已结束但栈未空");
                return error("语法错误: 输入已结束但栈未空");
            }
        }

        // 构建AST
        if (!astStack.isEmpty()) {
            Object result = astStack.pop();
            if (result instanceof ASTNode) {
                ast = (ASTNode) result;
            } else {
                return error("AST构建错误: 栈顶元素不是ASTNode");
            }
        }
        return ast;
    }

    // 处理AST构建
    private void handleASTConstruction(String nonTerminal, String production) {
        try {
            switch (nonTerminal) {
                case PROG:
                    // 程序节点，不需要特殊处理
                    break;

                case STMT:
                    // 语句节点，不需要特殊处理
                    break;

                case QUERY:
                    // 对于Query，我们不在这里处理，而是在所有组件准备好后处理
                    break;

                case CREATE_TABLE:
                    // CREATE TABLE语句，不在这里处理
                    break;

                case COL_DEF_LIST:
                    // 列定义列表
                    if (production.equals("ColDef ColDefListTail")) {
                        // 创建一个标记，表示开始构建列定义列表
                        astStack.push("COL_DEF_LIST_START");
                    }
                    break;

                case COL_DEF_LIST_TAIL:
                    // 列定义列表尾部，不需要特殊处理
                    break;

                case COL_DEF:
                    // 列定义，创建ColumnDefinition对象
                    if (production.equals("ID DataType ColumnConstraints")) {
                        astStack.push("COL_DEF_START");
                    }
                    break;

                case DATA_TYPE:
                    // 数据类型，标记开始
                    astStack.push("DATA_TYPE_START");
                    break;

                case COLUMN_CONSTRAINTS:
                    // 列约束，标记开始
                    if (!production.equals("ε")) {
                        astStack.push("CONSTRAINTS_START");
                    } else {
                        astStack.push(null); // 没有约束
                    }
                    break;

                case COLUMN_CONSTRAINT:
                    // 单个列约束，标记开始
                    astStack.push("CONSTRAINT_START");
                    break;

                case SELLIST:
                    // 选择列表
                    if (production.equals("ID SelListTail")) {
                        astStack.push("SEL_LIST_START");
                    }
                    break;

                case SELLIST_TAIL:
                    // 选择列表尾部，不需要特殊处理
                    break;

                case TBL:
                    // 表名，不需要特殊处理
                    break;

                case WHERE_CLAUSE:
                    // WHERE子句
                    if (production.equals("WHERE LogicalExpression")) {
                        // 标记WHERE子句开始
                        astStack.push("WHERE_START");
                    } else if (production.equals("ε")) {
                        astStack.push(null); // 没有WHERE子句
                    }
                    break;

                case CONDITION:
                    // 条件表达式
                    if (production.equals("ID Operator Value")) {
                        astStack.push("CONDITION_START");
                    }
                    break;

                case INSERT:
                    // INSERT语句，不在这里处理
                    break;

                case COL_LIST:
                    // 列列表
                    if (production.equals("ID ColListTail")) {
                        astStack.push("COL_LIST_START");
                    }
                    break;

                case COL_LIST_TAIL:
                    // 列列表尾部，不需要特殊处理
                    break;

                case VAL_LIST:
                    // 值列表
                    if (production.equals("Value ValListTail")) {
                        astStack.push("VAL_LIST_START");
                    }
                    break;

                case VAL_LIST_TAIL:
                    // 值列表尾部，不需要特殊处理
                    break;

                case DELETE:
                    // DELETE语句，不在这里处理
                    break;

                case OPERATOR:
                    // 操作符，不需要特殊处理（已在终结符匹配时处理）
                    break;

                case VALUE:
                    // 值，不需要特殊处理（已在终结符匹配时处理）
                    break;

                case LOGICAL_EXPRESSION:
                case LOGICAL_EXPRESSION_TAIL:
                case LOGICAL_TERM:
                case LOGICAL_TERM_TAIL:
                case LOGICAL_FACTOR:
                    // 逻辑表达式相关的非终结符，暂时不做特殊处理
                    // 在基本功能完成后再添加复杂的AST构建
                    break;

                default:
                    // 对于未处理的非终结符，不做任何操作
                    break;
            }
        } catch (Exception e) {
            recordStep("AST构建错误: " + e.getMessage());
            System.err.println("AST构建错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 判断是否为终结符
    private boolean isTerminal(String symbol) {
        return symbol.equals("SELECT") ||
                symbol.equals("CREATE") ||
                symbol.equals("TABLE") ||
                symbol.equals("INSERT") ||
                symbol.equals("INTO") ||
                symbol.equals("VALUES") ||
                symbol.equals("UPDATE") ||
                symbol.equals("SET") ||
                symbol.equals("DELETE") ||
                symbol.equals("FROM") ||
                symbol.equals("WHERE") ||
                symbol.equals("ORDER") ||
                symbol.equals("BY") ||
                symbol.equals("LIMIT") ||
                symbol.equals("ASC") ||
                symbol.equals("DESC") ||
                symbol.equals("AND") ||
                symbol.equals("OR") ||
                symbol.equals("NOT") ||
                symbol.equals("IN") ||
                symbol.equals("IS") ||
                symbol.equals("NULL") ||
                symbol.equals("ID") ||
                symbol.equals("CONSTANT") ||
                symbol.equals(";") ||
                symbol.equals(",") ||
                symbol.equals("(") ||
                symbol.equals(")") ||
                symbol.equals("=") ||
                symbol.equals(">") ||
                symbol.equals("<") ||
                symbol.equals(">=") ||
                symbol.equals("<=") ||
                symbol.equals("<>") ||
                symbol.equals("INT") ||
                symbol.equals("VARCHAR") ||
                symbol.equals("CHAR") ||
                symbol.equals("DATE") ||
                symbol.equals("FLOAT") ||
                symbol.equals("DOUBLE") ||
                symbol.equals("BOOLEAN") ||
                symbol.equals("PRIMARY") ||
                symbol.equals("KEY") ||
                symbol.equals("TRUE") ||
                symbol.equals("FALSE") ||
                symbol.equals("*") ||
                symbol.equals("$");
    }

    // 获取产生式（预测分析表）
    private String getProduction(String nonTerminal, String tokenType, String tokenValue) {
        switch (nonTerminal) {
            case PROG:
                if (tokenType.equals("KEYWORD") &&
                        (tokenValue.equalsIgnoreCase("SELECT") ||
                                tokenValue.equalsIgnoreCase("CREATE") ||
                                tokenValue.equalsIgnoreCase("INSERT") ||
                                tokenValue.equalsIgnoreCase("UPDATE") ||
                                tokenValue.equalsIgnoreCase("DELETE"))) {
                    return "Stmt ;";
                }
                break;

            case STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("SELECT")) {
                    return "Query";
                } else if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("CREATE")) {
                    return "CreateTable";
                } else if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("INSERT")) {
                    return "Insert";
                } else if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("UPDATE")) {
                    return "Update";
                } else if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("DELETE")) {
                    return "Delete";
                }
                break;

            case QUERY:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("SELECT")) {
                    return "SELECT SelList FROM Tbl WhereClause OrderClause LimitClause";
                }
                break;

            case CREATE_TABLE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("CREATE")) {
                    return "CREATE TABLE ID ( ColDefList )";
                }
                break;

            case INSERT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("INSERT")) {
                    return "INSERT INTO ID ( ColList ) VALUES ( ValList )";
                }
                break;

            case DELETE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("DELETE")) {
                    return "DELETE FROM Tbl WhereClause";
                }
                break;

            case "Update":
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("UPDATE")) {
                    return "UPDATE Tbl SET SetList WhereClause";
                }
                break;

            case "SetList":
                if (tokenType.equals("IDENTIFIER")) {
                    return "SetItem SetListTail";
                }
                break;

            case "SetListTail":
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", SetItem SetListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case "SetItem":
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID = Value";
                }
                break;

            case SELLIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID SelListTail";
                } else if (tokenType.equals("OPERATOR") && tokenValue.equals("*")) {
                    return "* SelListTail";
                }
                break;

            case SELLIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ID SelListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case TBL:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID";
                }
                break;

            case WHERE_CLAUSE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("WHERE")) {
                    return "WHERE LogicalExpression";
                } else {
                    return "ε"; // 空产生式
                }

            case ORDER_CLAUSE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("ORDER")) {
                    return "ORDER BY OrderList";
                } else {
                    return "ε"; // 空产生式
                }

            case ORDER_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "OrderItem OrderListTail";
                }
                break;

            case ORDER_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", OrderItem OrderListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case ORDER_ITEM:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID OrderDirection";
                }
                break;

            case ORDER_DIRECTION:
                if (tokenType.equals("KEYWORD") && 
                    (tokenValue.equalsIgnoreCase("ASC") || tokenValue.equalsIgnoreCase("DESC"))) {
                    return tokenValue.toUpperCase();
                } else {
                    return "ε"; // 默认ASC
                }

            case LIMIT_CLAUSE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("LIMIT")) {
                    return "LIMIT CONSTANT";
                } else {
                    return "ε"; // 空产生式
                }

            case CONDITION:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID Operator Value";
                }
                break;

            case OPERATOR:
                if (tokenType.equals("OPERATOR")) {
                    if (tokenValue.equals("=")) return "=";
                    if (tokenValue.equals(">")) return ">";
                    if (tokenValue.equals("<")) return "<";
                    if (tokenValue.equals(">=")) return ">=";
                    if (tokenValue.equals("<=")) return "<=";
                    if (tokenValue.equals("<>")) return "<>";
                }
                break;

            case VALUE:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID";
                } else if (tokenType.equals("CONSTANT")) {
                    return "CONSTANT";
                } else if (tokenType.equals("KEYWORD") && 
                          (tokenValue.equalsIgnoreCase("TRUE") || tokenValue.equalsIgnoreCase("FALSE"))) {
                    return tokenValue.toUpperCase();
                }
                break;

            case COL_DEF_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ColDef ColDefListTail";
                }
                break;

            case COL_DEF_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ColDef ColDefListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case COL_DEF:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID DataType ColumnConstraints";
                }
                break;

            case DATA_TYPE:
                if (tokenType.equals("KEYWORD")) {
                    if (tokenValue.equalsIgnoreCase("INT")) return "INT";
                    if (tokenValue.equalsIgnoreCase("VARCHAR")) return "VARCHAR ( CONSTANT )";
                    if (tokenValue.equalsIgnoreCase("CHAR")) return "CHAR ( CONSTANT )";
                    if (tokenValue.equalsIgnoreCase("DATE")) return "DATE";
                    if (tokenValue.equalsIgnoreCase("FLOAT")) return "FLOAT";
                    if (tokenValue.equalsIgnoreCase("DOUBLE")) return "DOUBLE";
                    if (tokenValue.equalsIgnoreCase("BOOLEAN")) return "BOOLEAN";
                }
                break;

            case COLUMN_CONSTRAINTS:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("PRIMARY")) {
                    return "ColumnConstraint ColumnConstraints";
                } else if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("NOT")) {
                    return "ColumnConstraint ColumnConstraints";
                } else {
                    return "ε"; // 空产生式 - 没有约束
                }

            case COLUMN_CONSTRAINT:
                if (tokenType.equals("KEYWORD")) {
                    if (tokenValue.equalsIgnoreCase("PRIMARY")) return "PRIMARY KEY";
                    if (tokenValue.equalsIgnoreCase("NOT")) return "NOT NULL";
                }
                break;

            case COL_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID ColListTail";
                }
                break;

            case COL_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ID ColListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case VAL_LIST:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT")) {
                    return "Value ValListTail";
                }
                break;

            case VAL_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", Value ValListTail";
                } else {
                    return "ε"; // 空产生式
                }

            case LOGICAL_EXPRESSION:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("KEYWORD") && 
                    (tokenValue.equalsIgnoreCase("NOT") || tokenValue.equals("("))) {
                    return "LogicalTerm LogicalExpressionTail";
                }
                break;

            case LOGICAL_EXPRESSION_TAIL:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("OR")) {
                    return "OR LogicalTerm LogicalExpressionTail";
                } else {
                    return "ε"; // 空产生式
                }

            case LOGICAL_TERM:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("KEYWORD") && 
                    (tokenValue.equalsIgnoreCase("NOT") || tokenValue.equals("("))) {
                    return "LogicalFactor LogicalTermTail";
                }
                break;

            case LOGICAL_TERM_TAIL:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("AND")) {
                    return "AND LogicalFactor LogicalTermTail";
                } else {
                    return "ε"; // 空产生式
                }

            case LOGICAL_FACTOR:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("NOT")) {
                    return "NOT LogicalFactor";
                } else if (tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "( LogicalExpression )";
                } else if (tokenType.equals("IDENTIFIER")) {
                    return "Condition";
                }
                break;
        }

        return null;
    }

    // 错误处理
    private ASTNode error(String message) {
        outputParseStep("语法错误: " + message);
        System.err.println(message);
        return null;
    }

    // 构建条件表达式（如果完整的话）
    private void buildConditionIfComplete() {
        // 检查栈顶的三个元素是否构成一个完整的条件：value, operator, column
        if (astStack.size() >= 3) {
            Object top1 = astStack.get(astStack.size() - 1);
            Object top2 = astStack.get(astStack.size() - 2);
            
            // 检查是否是操作符
            if (top1 instanceof String && isOperator((String) top1)) {
                // 此时栈结构可能是：column, operator（刚加入的）
                // 等待值被加入后再构建
                return;
            }
            
            // 检查是否是：value, operator, column的结构
            if (top2 instanceof String && isOperator((String) top2)) {
                Object value = astStack.pop();
                Object operator = astStack.pop();
                Object column = astStack.pop();
                
                ExpressionNode condition = new ExpressionNode(
                    column.toString(),
                    operator.toString(), 
                    value.toString()
                );
                astStack.push(condition);
            }
        }
    }
    
    // 辅助方法：检查是否为操作符
    private boolean isOperator(String str) {
        return str.equals("=") || str.equals(">") || str.equals("<") || 
               str.equals(">=") || str.equals("<=") || str.equals("<>");
    }

    // 构建最终的AST
    private void buildFinalAST() {
        try {
            // 语句解析完成，从AST栈构建最终的AST节点
            if (!astStack.isEmpty()) {
                // 首先处理条件表达式（如果存在）
                buildConditionIfComplete();
                
                // 找到AST节点的起始点
                Object rootNode = null;
                List<Object> elements = new ArrayList<>();
                
                // 收集所有栈元素，寻找根节点
                while (!astStack.isEmpty()) {
                    Object element = astStack.pop();
                    if (element instanceof SelectNode || element instanceof CreateTableNode || 
                        element instanceof InsertNode || element instanceof UpdateNode || element instanceof DeleteNode) {
                        rootNode = element;
                        break;
                    }
                    elements.add(0, element); // 保持顺序
                }
                
                if (rootNode instanceof SelectNode) {
                    SelectNode selectNode = (SelectNode) rootNode;
                    
                    // 解析栈中的元素来构建SELECT节点
                    List<String> columns = new ArrayList<>();
                    String tableName = "";
                    ExpressionNode whereClause = null;
                    
                    // 智能解析：基于SQL语法结构来识别不同部分
                    List<String> stringElements = new ArrayList<>();
                    for (Object element : elements) {
                        if (element instanceof String) {
                            String str = (String) element;
                            if (!str.contains("_START") && !str.equals("*")) { // 忽略标记，但保留*
                                stringElements.add(str);
                            } else if (str.equals("*")) {
                                columns.add("*");
                            }
                        } else if (element instanceof ExpressionNode) {
                            whereClause = (ExpressionNode) element;
                        }
                    }
                    
                    // 基于SQL语法来解析字符串元素
                    // SQL结构: SELECT columns FROM table [WHERE condition] [ORDER BY column [ASC|DESC]] [LIMIT number]
                    boolean foundFrom = false;
                    boolean foundOrderBy = false;
                    boolean foundLimit = false;
                    
                    for (int i = 0; i < stringElements.size(); i++) {
                        String str = stringElements.get(i);
                        
                        if (str.equalsIgnoreCase("FROM")) {
                            foundFrom = true;
                        } else if (str.equalsIgnoreCase("ORDER")) {
                            foundOrderBy = true;
                        } else if (str.equalsIgnoreCase("BY")) {
                            // 跳过BY关键字
                            continue;
                        } else if (str.equalsIgnoreCase("LIMIT")) {
                            foundLimit = true;
                        } else if (str.equalsIgnoreCase("ASC") || str.equalsIgnoreCase("DESC")) {
                            // 处理排序方向，暂时忽略
                            continue;
                        } else if (foundLimit) {
                            // LIMIT之后的数字
                            try {
                                selectNode.limit = Integer.parseInt(str);
                            } catch (NumberFormatException e) {
                                // 忽略无效的数字
                            }
                        } else if (foundOrderBy) {
                            // ORDER BY之后的列名
                            if (selectNode.orderBy == null) {
                                selectNode.orderBy = new executor.common.orderby.OrderByClause();
                            }
                            // 检查下一个元素是否是ASC/DESC
                            boolean ascending = true; // 默认ASC
                            if (i + 1 < stringElements.size()) {
                                String next = stringElements.get(i + 1);
                                if (next.equalsIgnoreCase("DESC")) {
                                    ascending = false;
                                    i++; // 跳过DESC
                                } else if (next.equalsIgnoreCase("ASC")) {
                                    i++; // 跳过ASC
                                }
                            }
                            selectNode.orderBy.addItem(str, ascending);
                        } else if (foundFrom && tableName.isEmpty()) {
                            // FROM之后的第一个标识符是表名
                            tableName = str;
                        } else if (!foundFrom) {
                            // FROM之前的标识符是列名
                            if (!columns.contains("*")) { // 如果不是SELECT *
                                columns.add(str);
                            }
                        }
                    }
                    
                    selectNode.columns = columns;
                    selectNode.tableName = tableName;
                    selectNode.whereClause = whereClause;
                    
                    astStack.push(selectNode);
                }
                else if (rootNode instanceof CreateTableNode) {
                    CreateTableNode createNode = (CreateTableNode) rootNode;
                    
                    // 解析CREATE TABLE的元素
                    List<String> columnNames = new ArrayList<>();
                    List<String> dataTypes = new ArrayList<>();
                    String tableName = "";
                    
                    // 清理标记并提取有用信息
                    List<Object> cleanedElements = new ArrayList<>();
                    for (Object element : elements) {
                        if (element instanceof String) {
                            String str = (String) element;
                            if (!str.contains("_START")) { // 忽略标记
                                cleanedElements.add(str);
                            }
                        } else if (element != null) {
                            cleanedElements.add(element);
                        }
                    }
                    
                    // 对于CREATE TABLE语句，解析列定义
                    if (!cleanedElements.isEmpty()) {
                        tableName = (String) cleanedElements.get(0);
                        
                        // 解析列定义：列名 数据类型 [约束]
                        // 对于VARCHAR(50)这样的类型，需要组合多个元素
                        List<String> currentColumnElements = new ArrayList<>();
                        String currentColumnName = null;
                        
                        for (int i = 1; i < cleanedElements.size(); i++) {
                            String element = (String) cleanedElements.get(i);
                            
                            // 如果是潜在的列名（通常是标识符）
                            if (currentColumnName == null && isIdentifier(element)) {
                                currentColumnName = element;
                                currentColumnElements.clear();
                            }
                            // 如果是数据类型关键字
                            else if (isDataTypeKeyword(element)) {
                                currentColumnElements.add(element);
                                
                                // 检查是否需要长度参数
                                if (element.equalsIgnoreCase("VARCHAR") || element.equalsIgnoreCase("CHAR")) {
                                    // 跳过括号和长度，直接组合
                                    if (i + 2 < cleanedElements.size() && 
                                        isNumeric((String) cleanedElements.get(i + 2))) {
                                        String length = (String) cleanedElements.get(i + 2);
                                        currentColumnElements.add("(" + length + ")");
                                        i += 3; // 跳过 '(', length, ')'
                                    }
                                }
                                
                                // 完成当前列定义
                                if (currentColumnName != null) {
                                    columnNames.add(currentColumnName);
                                    dataTypes.add(String.join("", currentColumnElements));
                                    currentColumnName = null;
                                    currentColumnElements.clear();
                                }
                            }
                            // 如果是其他数据类型（不需要长度参数）
                            else if (element.equalsIgnoreCase("INT") || 
                                    element.equalsIgnoreCase("FLOAT") || 
                                    element.equalsIgnoreCase("DOUBLE") ||
                                    element.equalsIgnoreCase("BOOLEAN") ||
                                    element.equalsIgnoreCase("DATE")) {
                                if (currentColumnName != null) {
                                    columnNames.add(currentColumnName);
                                    dataTypes.add(element);
                                    currentColumnName = null;
                                    currentColumnElements.clear();
                                }
                            }
                        }
                    }
                    
                    createNode.tableName = tableName;
                    for (int i = 0; i < columnNames.size(); i++) {
                        createNode.columns.add(new ColumnDefinition(columnNames.get(i), dataTypes.get(i)));
                    }
                    
                    astStack.push(createNode);
                }
                else if (rootNode instanceof InsertNode) {
                    InsertNode insertNode = (InsertNode) rootNode;
                    
                    // 解析INSERT的元素
                    List<String> columns = new ArrayList<>();
                    List<Object> values = new ArrayList<>();
                    String tableName = "";
                    
                    // 清理标记并提取有用信息
                    List<Object> cleanedElements = new ArrayList<>();
                    for (Object element : elements) {
                        if (element instanceof String) {
                            String str = (String) element;
                            if (!str.contains("_START")) { // 忽略标记
                                cleanedElements.add(str);
                            }
                        } else if (element != null) {
                            cleanedElements.add(element);
                        }
                    }
                    
                    // 对于INSERT语句，需要分析结构
                    // INSERT INTO table (col1, col2) VALUES (val1, val2)
                    if (!cleanedElements.isEmpty()) {
                        tableName = (String) cleanedElements.get(0);
                        
                        // 简化处理：假设前半部分是列名，后半部分是值
                        int mid = cleanedElements.size() / 2;
                        for (int i = 1; i <= mid; i++) {
                            if (i < cleanedElements.size()) {
                                columns.add((String) cleanedElements.get(i));
                            }
                        }
                        for (int i = mid + 1; i < cleanedElements.size(); i++) {
                            values.add(cleanedElements.get(i));
                        }
                    }
                    
                    insertNode.tableName = tableName;
                    insertNode.columns = columns;
                    insertNode.values = values;
                    
                    astStack.push(insertNode);
                }
                else if (rootNode instanceof DeleteNode) {
                    DeleteNode deleteNode = (DeleteNode) rootNode;
                    
                    // 解析DELETE的元素
                    String tableName = "";
                    ExpressionNode whereClause = null;
                    
                    // 智能解析：基于SQL语法结构来识别不同部分
                    List<String> stringElements = new ArrayList<>();
                    for (Object element : elements) {
                        if (element instanceof String) {
                            String str = (String) element;
                            if (!str.contains("_START")) { // 忽略标记
                                stringElements.add(str);
                            }
                        } else if (element instanceof ExpressionNode) {
                            whereClause = (ExpressionNode) element;
                        }
                    }
                    
                    // 基于SQL语法来解析字符串元素
                    // SQL结构: DELETE FROM table [WHERE condition]
                    boolean foundFrom = false;
                    
                    for (String str : stringElements) {
                        if (str.equalsIgnoreCase("FROM")) {
                            foundFrom = true;
                        } else if (str.equalsIgnoreCase("WHERE")) {
                            // 跳过WHERE关键字
                            continue;
                        } else if (foundFrom && tableName.isEmpty()) {
                            // FROM之后的第一个标识符是表名
                            tableName = str;
                        }
                    }
                    
                    deleteNode.tableName = tableName;
                    deleteNode.whereClause = whereClause;
                    
                    astStack.push(deleteNode);
                }
                else if (rootNode instanceof UpdateNode) {
                    UpdateNode updateNode = (UpdateNode) rootNode;
                    
                    // 解析UPDATE的元素
                    String tableName = "";
                    Map<String, Object> setValues = new HashMap<>();
                    ExpressionNode whereClause = null;
                    
                    // 智能解析：基于SQL语法结构来识别不同部分
                    List<String> stringElements = new ArrayList<>();
                    for (Object element : elements) {
                        if (element instanceof String) {
                            String str = (String) element;
                            if (!str.contains("_START")) { // 忽略标记
                                stringElements.add(str);
                            }
                        } else if (element instanceof ExpressionNode) {
                            whereClause = (ExpressionNode) element;
                        }
                    }
                    
                    // 基于SQL语法来解析字符串元素
                    // SQL结构: UPDATE table SET column=value [, column=value] [WHERE condition]
                    boolean foundSet = false;
                    boolean foundWhere = false;
                    String currentColumn = null;
                    
                    for (int i = 0; i < stringElements.size(); i++) {
                        String str = stringElements.get(i);
                        
                        if (str.equalsIgnoreCase("SET")) {
                            foundSet = true;
                        } else if (str.equalsIgnoreCase("WHERE")) {
                            foundWhere = true;
                            break; // WHERE之后的处理由ExpressionNode完成
                        } else if (!foundSet && tableName.isEmpty()) {
                            // SET之前的第一个标识符是表名
                            tableName = str;
                        } else if (foundSet && !foundWhere) {
                            // SET之后的部分解析为 column=value 对
                            if (currentColumn == null) {
                                currentColumn = str; // 列名
                            } else if (i + 1 < stringElements.size() && 
                                     stringElements.get(i).equals("=")) {
                                // 跳过=号，下一个是值
                                continue;
                            } else {
                                // 这是值
                                setValues.put(currentColumn, str);
                                currentColumn = null; // 重置以处理下一个列值对
                            }
                        }
                    }
                    
                    updateNode.tableName = tableName;
                    updateNode.setValues = setValues;
                    updateNode.whereClause = whereClause;
                    
                    astStack.push(updateNode);
                }
                // 可以添加其他节点类型的处理逻辑
            }
        } catch (Exception e) {
            System.err.println("最终AST构建错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 获取分析输出
    public String getOutput() {
        return output.toString();
    }
    
    // 辅助方法：检查是否为标识符
    private boolean isIdentifier(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        
        // 简单的标识符检查：以字母或下划线开头，后跟字母、数字或下划线
        char first = str.charAt(0);
        if (!Character.isLetter(first) && first != '_') {
            return false;
        }
        
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        
        // 不应该是关键字
        return !isDataTypeKeyword(str);
    }
    
    // 辅助方法：检查是否为数据类型关键字
    private boolean isDataTypeKeyword(String str) {
        if (str == null) {
            return false;
        }
        String upper = str.toUpperCase();
        return upper.equals("INT") || upper.equals("INTEGER") ||
               upper.equals("VARCHAR") || upper.equals("CHAR") ||
               upper.equals("FLOAT") || upper.equals("DOUBLE") ||
               upper.equals("BOOLEAN") || upper.equals("DATE") ||
               upper.equals("DATETIME") || upper.equals("TEXT");
    }
    
    // 辅助方法：检查是否为数字
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(str);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }
}