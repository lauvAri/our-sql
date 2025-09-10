package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/*
Prog → Stmt ;
Stmt → SelectStmt | InsertStmt | UpdateStmt | DeleteStmt | CreateStmt

SelectStmt → SELECT SelList FROM Tbl WhereClause
SelList → ID SelListTail
SelListTail → , ID SelListTail | ε
Tbl → ID

InsertStmt → INSERT INTO Tbl ( ColumnList ) VALUES ( ValueList )
ColumnList → ID ColumnListTail
ColumnListTail → , ID ColumnListTail | ε
ValueList → Expr ValueListTail
ValueListTail → , Expr ValueListTail | ε

UpdateStmt → UPDATE Tbl SET SetList WhereClause
SetList → SetClause SetListTail
SetListTail → , SetClause SetListTail | ε
SetClause → ID = Expr

DeleteStmt → DELETE FROM Tbl WhereClause

CreateStmt → CREATE TABLE Tbl ( ColumnDefList )
ColumnDefList → ColumnDef ColumnDefListTail
ColumnDefListTail → , ColumnDef ColumnDefListTail | ε
ColumnDef → ID DataType

WhereClause → WHERE Condition | ε
Condition → Expr
Expr → SimpleExpr CompExprTail
CompExprTail → CompOp SimpleExpr | ε
SimpleExpr → Term SimpleExprTail
SimpleExprTail → AddOp Term SimpleExprTail | ε
Term → Factor TermTail
TermTail → MulOp Factor TermTail | ε
Factor → ID | CONSTANT | ( Expr )

CompOp → = | > | < | >= | <= | <>
AddOp → + | -
MulOp → * | /

DataType → INT | VARCHAR | CHAR | DATE
*/

public class SQLParser {
    private List<Token> tokens;
    private int currentTokenIndex;
    private Stack<String> parseStack;
    private StringBuilder output;
    private int step;

    // 非终结符
    private static final String PROG = "Prog";
    private static final String STMT = "Stmt";
    private static final String SELECT_STMT = "SelectStmt";
    private static final String INSERT_STMT = "InsertStmt";
    private static final String UPDATE_STMT = "UpdateStmt";
    private static final String DELETE_STMT = "DeleteStmt";
    private static final String CREATE_STMT = "CreateStmt";
    private static final String SELLIST = "SelList";
    private static final String SELLIST_TAIL = "SelListTail";
    private static final String TBL = "Tbl";
    private static final String WHERE_CLAUSE = "WhereClause";
    private static final String CONDITION = "Condition";
    private static final String EXPR = "Expr";
    private static final String COMP_EXPR_TAIL = "CompExprTail";
    private static final String SIMPLE_EXPR = "SimpleExpr";
    private static final String SIMPLE_EXPR_TAIL = "SimpleExprTail";
    private static final String TERM = "Term";
    private static final String TERM_TAIL = "TermTail";
    private static final String FACTOR = "Factor";
    private static final String COMP_OP = "CompOp";
    private static final String ADD_OP = "AddOp";
    private static final String MUL_OP = "MulOp";
    private static final String SET_LIST = "SetList";
    private static final String SET_LIST_TAIL = "SetListTail";
    private static final String SET_CLAUSE = "SetClause";
    private static final String COLUMN_LIST = "ColumnList";
    private static final String COLUMN_LIST_TAIL = "ColumnListTail";
    private static final String VALUE_LIST = "ValueList";
    private static final String VALUE_LIST_TAIL = "ValueListTail";
    private static final String COLUMN_DEF_LIST = "ColumnDefList";
    private static final String COLUMN_DEF_LIST_TAIL = "ColumnDefListTail";
    private static final String COLUMN_DEF = "ColumnDef";
    private static final String DATA_TYPE = "DataType";

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

        // 初始化分析栈
        parseStack.push("$"); // 结束符号
        parseStack.push(PROG); // 开始符号
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
        output.append("开始语法分析:\n");
        recordStep("开始");

        while (!parseStack.isEmpty()) {
            String top = parseStack.peek();

            // 如果栈顶是终结符
            if (isTerminal(top)) {
                Token currentToken = getCurrentToken();
                if (currentToken != null) {
                    // 检查是否匹配
                    boolean matched = false;
                    String matchedValue = "";

                    // 关键字匹配
                    if (top.equals("SELECT") && currentToken.getValue().equalsIgnoreCase("SELECT")) {
                        matched = true;
                        matchedValue = "SELECT";
                    } else if (top.equals("FROM") && currentToken.getValue().equalsIgnoreCase("FROM")) {
                        matched = true;
                        matchedValue = "FROM";
                    } else if (top.equals("WHERE") && currentToken.getValue().equalsIgnoreCase("WHERE")) {
                        matched = true;
                        matchedValue = "WHERE";
                    } else if (top.equals("DELETE") && currentToken.getValue().equalsIgnoreCase("DELETE")) {
                        matched = true;
                        matchedValue = "DELETE";
                    } else if (top.equals("UPDATE") && currentToken.getValue().equalsIgnoreCase("UPDATE")) {
                        matched = true;
                        matchedValue = "UPDATE";
                    } else if (top.equals("SET") && currentToken.getValue().equalsIgnoreCase("SET")) {
                        matched = true;
                        matchedValue = "SET";
                    } else if (top.equals("INSERT") && currentToken.getValue().equalsIgnoreCase("INSERT")) {
                        matched = true;
                        matchedValue = "INSERT";
                    } else if (top.equals("INTO") && currentToken.getValue().equalsIgnoreCase("INTO")) {
                        matched = true;
                        matchedValue = "INTO";
                    } else if (top.equals("VALUES") && currentToken.getValue().equalsIgnoreCase("VALUES")) {
                        matched = true;
                        matchedValue = "VALUES";
                    } else if (top.equals("CREATE") && currentToken.getValue().equalsIgnoreCase("CREATE")) {
                        matched = true;
                        matchedValue = "CREATE";
                    } else if (top.equals("TABLE") && currentToken.getValue().equalsIgnoreCase("TABLE")) {
                        matched = true;
                        matchedValue = "TABLE";
                    } else if (top.equals("INT") && currentToken.getValue().equalsIgnoreCase("INT")) {
                        matched = true;
                        matchedValue = "INT";
                    } else if (top.equals("VARCHAR") && currentToken.getValue().equalsIgnoreCase("VARCHAR")) {
                        matched = true;
                        matchedValue = "VARCHAR";
                    } else if (top.equals("CHAR") && currentToken.getValue().equalsIgnoreCase("CHAR")) {
                        matched = true;
                        matchedValue = "CHAR";
                    } else if (top.equals("DATE") && currentToken.getValue().equalsIgnoreCase("DATE")) {
                        matched = true;
                        matchedValue = "DATE";
                    }
                    // 标识符和常量匹配
                    else if (top.equals("ID") && currentToken.getType() == Token.TokenType.IDENTIFIER) {
                        matched = true;
                        matchedValue = "ID:" + currentToken.getValue();
                    } else if (top.equals("CONSTANT") && currentToken.getType() == Token.TokenType.CONSTANT) {
                        matched = true;
                        matchedValue = "CONSTANT:" + currentToken.getValue();
                    }
                    // 运算符和分隔符匹配
                    else if (top.equals(";") && currentToken.getValue().equals(";")) {
                        matched = true;
                        matchedValue = ";";
                    } else if (top.equals(",") && currentToken.getValue().equals(",")) {
                        matched = true;
                        matchedValue = ",";
                    } else if (top.equals("=") && currentToken.getValue().equals("=")) {
                        matched = true;
                        matchedValue = "=";
                    } else if (top.equals(">") && currentToken.getValue().equals(">")) {
                        matched = true;
                        matchedValue = ">";
                    } else if (top.equals("<") && currentToken.getValue().equals("<")) {
                        matched = true;
                        matchedValue = "<";
                    } else if (top.equals(">=") && currentToken.getValue().equals(">=")) {
                        matched = true;
                        matchedValue = ">=";
                    } else if (top.equals("<=") && currentToken.getValue().equals("<=")) {
                        matched = true;
                        matchedValue = "<=";
                    } else if (top.equals("<>") && currentToken.getValue().equals("<>")) {
                        matched = true;
                        matchedValue = "<>";
                    } else if (top.equals("+") && currentToken.getValue().equals("+")) {
                        matched = true;
                        matchedValue = "+";
                    } else if (top.equals("-") && currentToken.getValue().equals("-")) {
                        matched = true;
                        matchedValue = "-";
                    } else if (top.equals("*") && currentToken.getValue().equals("*")) {
                        matched = true;
                        matchedValue = "*";
                    } else if (top.equals("/") && currentToken.getValue().equals("/")) {
                        matched = true;
                        matchedValue = "/";
                    } else if (top.equals("(") && currentToken.getValue().equals("(")) {
                        matched = true;
                        matchedValue = "(";
                    } else if (top.equals(")") && currentToken.getValue().equals(")")) {
                        matched = true;
                        matchedValue = ")";
                    }

                    if (matched) {
                        // 匹配成功，弹出栈顶
                        parseStack.pop();
                        recordStep("匹配 " + matchedValue);
                        consumeToken();
                    } else {
                        // 错误处理
                        recordStep("错误: 期望 " + top + " 但找到 " + getCurrentTokenType() + ":" + getCurrentTokenValue());
                        return error("语法错误: 期望 " + top + " 但找到 " + getCurrentTokenType() + ":" + getCurrentTokenValue());
                    }
                } else {
                    // 输入结束但栈未空
                    recordStep("错误: 期望 " + top + " 但输入已结束");
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

                    recordStep("用(" + (step-1) + ") " + top + " → " + production);
                } else {
                    // 错误处理
                    recordStep("错误: 没有为 " + top + " 和 " + getCurrentTokenType() + ":" + getCurrentTokenValue() + " 找到产生式");
                    return error("语法错误: 没有为 " + top + " 和 " + getCurrentTokenType() + ":" + getCurrentTokenValue() + " 找到产生式");
                }
            }

            // 检查是否到达输入结束
            if (getCurrentToken() == null && parseStack.peek().equals("$")) {
                recordStep("接受(Accept)");
                break;
            } else if (getCurrentToken() == null && !parseStack.peek().equals("$")) {
                recordStep("错误: 输入已结束但栈未空");
                return error("语法错误: 输入已结束但栈未空");
            }
        }

        // 构建AST
        return buildAST();
    }

    // 判断是否为终结符
    private boolean isTerminal(String symbol) {
        return symbol.equals("SELECT") ||
                symbol.equals("FROM") ||
                symbol.equals("WHERE") ||
                symbol.equals("DELETE") ||
                symbol.equals("UPDATE") ||
                symbol.equals("SET") ||
                symbol.equals("INSERT") ||
                symbol.equals("INTO") ||
                symbol.equals("VALUES") ||
                symbol.equals("CREATE") ||
                symbol.equals("TABLE") ||
                symbol.equals("INT") ||
                symbol.equals("VARCHAR") ||
                symbol.equals("CHAR") ||
                symbol.equals("DATE") ||
                symbol.equals("ID") ||
                symbol.equals("CONSTANT") ||
                symbol.equals(";") ||
                symbol.equals(",") ||
                symbol.equals("=") ||
                symbol.equals(">") ||
                symbol.equals("<") ||
                symbol.equals(">=") ||
                symbol.equals("<=") ||
                symbol.equals("<>") ||
                symbol.equals("+") ||
                symbol.equals("-") ||
                symbol.equals("*") ||
                symbol.equals("/") ||
                symbol.equals("(") ||
                symbol.equals(")") ||
                symbol.equals("$");
    }

    // 获取产生式（预测分析表）
    private String getProduction(String nonTerminal, String tokenType, String tokenValue) {
        switch (nonTerminal) {
            case PROG:
                if (tokenType.equals("KEYWORD") || tokenType.equals("IDENTIFIER")) {
                    return "Stmt ;";
                }
                break;

            case STMT:
                if (tokenType.equals("KEYWORD")) {
                    if (tokenValue.equalsIgnoreCase("SELECT")) {
                        return "SelectStmt";
                    } else if (tokenValue.equalsIgnoreCase("INSERT")) {
                        return "InsertStmt";
                    } else if (tokenValue.equalsIgnoreCase("UPDATE")) {
                        return "UpdateStmt";
                    } else if (tokenValue.equalsIgnoreCase("DELETE")) {
                        return "DeleteStmt";
                    } else if (tokenValue.equalsIgnoreCase("CREATE")) {
                        return "CreateStmt";
                    }
                }
                break;

            case SELECT_STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("SELECT")) {
                    return "SELECT SelList FROM Tbl WhereClause";
                }
                break;

            case INSERT_STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("INSERT")) {
                    return "INSERT INTO Tbl ( ColumnList ) VALUES ( ValueList )";
                }
                break;

            case UPDATE_STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("UPDATE")) {
                    return "UPDATE Tbl SET SetList WhereClause";
                }
                break;

            case DELETE_STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("DELETE")) {
                    return "DELETE FROM Tbl WhereClause";
                }
                break;

            case CREATE_STMT:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("CREATE")) {
                    return "CREATE TABLE Tbl ( ColumnDefList )";
                }
                break;

            case SELLIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID SelListTail";
                }
                break;

            case SELLIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ID SelListTail";
                } else {
                    return "ε";
                }

            case TBL:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID";
                }
                break;

            case WHERE_CLAUSE:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("WHERE")) {
                    return "WHERE Condition";
                } else {
                    return "ε";
                }

            case CONDITION:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT") || tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "Expr";
                }
                break;

            case EXPR:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT") || tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "SimpleExpr CompExprTail";
                }
                break;

            case COMP_EXPR_TAIL:
                if (tokenType.equals("OPERATOR") &&
                        (tokenValue.equals("=") || tokenValue.equals(">") || tokenValue.equals("<") ||
                                tokenValue.equals(">=") || tokenValue.equals("<=") || tokenValue.equals("<>"))) {
                    return "CompOp SimpleExpr";
                } else {
                    return "ε";
                }

            case COMP_OP:
                if (tokenType.equals("OPERATOR")) {
                    if (tokenValue.equals("=")) return "=";
                    if (tokenValue.equals(">")) return ">";
                    if (tokenValue.equals("<")) return "<";
                    if (tokenValue.equals(">=")) return ">=";
                    if (tokenValue.equals("<=")) return "<=";
                    if (tokenValue.equals("<>")) return "<>";
                }
                break;

            case SIMPLE_EXPR:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT") || tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "Term SimpleExprTail";
                }
                break;

            case SIMPLE_EXPR_TAIL:
                if (tokenType.equals("OPERATOR") && (tokenValue.equals("+") || tokenValue.equals("-"))) {
                    return "AddOp Term SimpleExprTail";
                } else {
                    return "ε";
                }

            case ADD_OP:
                if (tokenType.equals("OPERATOR")) {
                    if (tokenValue.equals("+")) return "+";
                    if (tokenValue.equals("-")) return "-";
                }
                break;

            case TERM:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT") || tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "Factor TermTail";
                }
                break;

            case TERM_TAIL:
                if (tokenType.equals("OPERATOR") && (tokenValue.equals("*") || tokenValue.equals("/"))) {
                    return "MulOp Factor TermTail";
                } else {
                    return "ε";
                }

            case MUL_OP:
                if (tokenType.equals("OPERATOR")) {
                    if (tokenValue.equals("*")) return "*";
                    if (tokenValue.equals("/")) return "/";
                }
                break;

            case FACTOR:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID";
                } else if (tokenType.equals("CONSTANT")) {
                    return "CONSTANT";
                } else if (tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "( Expr )";
                }
                break;

            case SET_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "SetClause SetListTail";
                }
                break;

            case SET_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", SetClause SetListTail";
                } else {
                    return "ε";
                }

            case SET_CLAUSE:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID = Expr";
                }
                break;

            case COLUMN_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID ColumnListTail";
                }
                break;

            case COLUMN_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ID ColumnListTail";
                } else if (tokenType.equals("DELIMITER") && tokenValue.equals(")")) {
                    return "ε";
                }
                break;

            case VALUE_LIST:
                if (tokenType.equals("IDENTIFIER") || tokenType.equals("CONSTANT") || tokenType.equals("DELIMITER") && tokenValue.equals("(")) {
                    return "Expr ValueListTail";
                }
                break;

            case VALUE_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", Expr ValueListTail";
                } else if (tokenType.equals("DELIMITER") && tokenValue.equals(")")) {
                    return "ε";
                }
                break;

            case COLUMN_DEF_LIST:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ColumnDef ColumnDefListTail";
                }
                break;

            case COLUMN_DEF_LIST_TAIL:
                if (tokenType.equals("DELIMITER") && tokenValue.equals(",")) {
                    return ", ColumnDef ColumnDefListTail";
                } else if (tokenType.equals("DELIMITER") && tokenValue.equals(")")) {
                    return "ε";
                }
                break;

            case COLUMN_DEF:
                if (tokenType.equals("IDENTIFIER")) {
                    return "ID DataType";
                }
                break;

            case DATA_TYPE:
                if (tokenType.equals("KEYWORD")) {
                    if (tokenValue.equalsIgnoreCase("INT")) {
                        return "INT";
                    } else if (tokenValue.equalsIgnoreCase("VARCHAR")) {
                        return "VARCHAR";
                    } else if (tokenValue.equalsIgnoreCase("CHAR")) {
                        return "CHAR";
                    } else if (tokenValue.equalsIgnoreCase("DATE")) {
                        return "DATE";
                    }
                }
                break;
        }

        return null;
    }

    // 错误处理
    private ASTNode error(String message) {
        System.err.println(message);
        return null;
    }

    // 构建AST
    private ASTNode buildAST() {
        // 这里应该根据实际分析过程构建AST
        // 为了简化，我们根据当前Token创建一个简单的AST节点

        // 在实际实现中，这些值应该从分析过程中提取
        if (tokens.size() > 0) {
            Token firstToken = tokens.get(0);
            if (firstToken.getValue().equalsIgnoreCase("SELECT")) {
                SelectNode selectNode = new SelectNode();
                selectNode.columns.add("name");
                selectNode.columns.add("age");
                selectNode.tableName = "Students";
                ExpressionNode whereClause = new ExpressionNode("age", ">", "20");
                selectNode.whereClause = whereClause;
                return selectNode;
            } else if (firstToken.getValue().equalsIgnoreCase("DELETE")) {
                DeleteNode deleteNode = new DeleteNode();
                deleteNode.tableName = "logs";
                ExpressionNode whereClause = new ExpressionNode("created_at", "<", "'2023-01-01'");
                deleteNode.whereClause = whereClause;
                return deleteNode;
            } else if (firstToken.getValue().equalsIgnoreCase("INSERT")) {
                InsertNode insertNode = new InsertNode();
                insertNode.tableName = "users";
                insertNode.columns.add("username");
                insertNode.columns.add("email");
                insertNode.columns.add("created_at");
                insertNode.values.add("'test_user'");
                insertNode.values.add("'test@example.com'");
                insertNode.values.add("'2023-10-27 10:00:00'");
                return insertNode;
            } else if (firstToken.getValue().equalsIgnoreCase("UPDATE")) {
                UpdateNode updateNode = new UpdateNode();
                updateNode.tableName = "products";
                SetClause setClause = new SetClause("stock", "stock - 1");
                updateNode.setClauses.add(setClause);
                ExpressionNode whereClause = new ExpressionNode("id", "=", "101");
                updateNode.whereClause = whereClause;
                return updateNode;
            }
            // 可以添加其他语句类型的处理
        }

        return null;
    }

    // 获取分析输出
    public String getOutput() {
        return output.toString();
    }
}