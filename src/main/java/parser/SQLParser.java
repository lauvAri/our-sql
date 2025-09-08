package parser;
/*文法规则
    Prog → Query ;
    Query → SELECT SelList FROM Tbl WhereClause
    SelList → ID SelListTail
    SelListTail → , ID SelListTail | ε
    Tbl → ID
    WhereClause → WHERE Condition | ε
    Condition → ID Operator Value
    Value → ID | CONSTANT
    Operator → = | > | < | >= | <= | <>
*/
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SQLParser {
    private List<Token> tokens;
    private int currentTokenIndex;
    private Stack<String> parseStack;
    private StringBuilder output;
    private int step;

    // 非终结符
    private static final String PROG = "Prog";
    private static final String QUERY = "Query";
    private static final String SELLIST = "SelList";
    private static final String SELLIST_TAIL = "SelListTail";
    private static final String TBL = "Tbl";
    private static final String WHERE_CLAUSE = "WhereClause";
    private static final String CONDITION = "Condition";
    private static final String VALUE = "Value";
    private static final String OPERATOR = "Operator";

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

                    if (top.equals("SELECT") && currentToken.getValue().equalsIgnoreCase("SELECT")) {
                        matched = true;
                        matchedValue = "SELECT";
                    } else if (top.equals("FROM") && currentToken.getValue().equalsIgnoreCase("FROM")) {
                        matched = true;
                        matchedValue = "FROM";
                    } else if (top.equals("WHERE") && currentToken.getValue().equalsIgnoreCase("WHERE")) {
                        matched = true;
                        matchedValue = "WHERE";
                    } else if (top.equals("ID") && currentToken.getType() == Token.TokenType.IDENTIFIER) {
                        matched = true;
                        matchedValue = "ID:" + currentToken.getValue();
                    } else if (top.equals("CONSTANT") && currentToken.getType() == Token.TokenType.CONSTANT) {
                        matched = true;
                        matchedValue = "CONSTANT:" + currentToken.getValue();
                    } else if (top.equals(";") && currentToken.getValue().equals(";")) {
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
                symbol.equals("$");
    }

    // 获取产生式（预测分析表）
    private String getProduction(String nonTerminal, String tokenType, String tokenValue) {
        switch (nonTerminal) {
            case PROG:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("SELECT")) {
                    return "Query ;";
                }
                break;

            case QUERY:
                if (tokenType.equals("KEYWORD") && tokenValue.equalsIgnoreCase("SELECT")) {
                    return "SELECT SelList FROM Tbl WhereClause";
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
                    return "ε"; // 空产生式
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
        // 为了简化，我们创建一个简单的SELECT AST节点
        SelectNode selectNode = new SelectNode();

        // 在实际实现中，这些值应该从分析过程中提取
        selectNode.columns.add("name");
        selectNode.columns.add("age");
        selectNode.tableName = "Students";

        ExpressionNode whereClause = new ExpressionNode("age", ">", "20");
        selectNode.whereClause = whereClause;

        return selectNode;
    }

    // 获取分析输出
    public String getOutput() {
        return output.toString();
    }

    // 测试方法
    public static void main(String[] args) {
        // 创建测试Token列表
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.TokenType.KEYWORD, "SELECT", 1, 1));
        tokens.add(new Token(Token.TokenType.IDENTIFIER, "name", 1, 8));
        tokens.add(new Token(Token.TokenType.DELIMITER, ",", 1, 12));
        tokens.add(new Token(Token.TokenType.IDENTIFIER, "age", 1, 14));
        tokens.add(new Token(Token.TokenType.KEYWORD, "FROM", 2, 1));
        tokens.add(new Token(Token.TokenType.IDENTIFIER, "Students", 2, 6));
        tokens.add(new Token(Token.TokenType.KEYWORD, "WHERE", 3, 1));
        tokens.add(new Token(Token.TokenType.IDENTIFIER, "age", 3, 7));
        tokens.add(new Token(Token.TokenType.OPERATOR, ">", 3, 11));
        tokens.add(new Token(Token.TokenType.CONSTANT, "20", 3, 13));
        tokens.add(new Token(Token.TokenType.DELIMITER, ";", 3, 15));

        // 创建语法分析器
        SQLParser parser = new SQLParser(tokens);
        ASTNode ast = parser.parse();

        // 输出分析结果
        System.out.println(parser.getOutput());

        if (ast != null) {
            System.out.println("生成的AST:");
            System.out.println(ast.toString());
        }
    }
}