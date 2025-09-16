package parser;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Token类表示词法单元
class Token {
    public enum TokenType {
        KEYWORD,      // 关键字
        IDENTIFIER,   // 标识符
        CONSTANT,     // 常量
        OPERATOR,     // 运算符
        DELIMITER,    // 分隔符
        COMMENT,      // 注释
        ERROR         // 错误
    }

    private TokenType type;
    private String value;
    private int line;
    private int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %d, %d]", type, value, line, column);
    }

    // Getter方法
    public TokenType getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}

// 词法分析器类
public class SQLLexer {
    private String input;
    private int currentPos;
    private int currentLine;
    private int currentColumn;

    // SQL关键字列表
    private static final String[] KEYWORDS = {
            "SELECT", "FROM", "WHERE", "CREATE", "TABLE", "INSERT", "INTO",
            "VALUES", "DELETE", "UPDATE", "SET", "AND", "OR", "NOT", "NULL",
            "INT", "VARCHAR", "CHAR", "DATE", "FLOAT", "DOUBLE", "BOOLEAN", 
            "PRIMARY", "KEY", "DROP", "ALTER", "CHECK", "IN", "JOIN", "ON",
            "TRUE", "FALSE", "ORDER", "BY", "LIMIT", "ASC", "DESC"
    };

    // 运算符列表
    private static final String[] OPERATORS = {
            "=", ">", "<", ">=", "<=", "<>", "!=", "+", "-", "*", "/", "%"
    };

    // 分隔符列表
    private static final String[] DELIMITERS = {
            ",", ";", "(", ")", "{", "}"
    };

    public SQLLexer(String input) {
        this.input = input;
        this.currentPos = 0;
        this.currentLine = 1;
        this.currentColumn = 1;
    }

    // 判断是否为关键字
    private boolean isKeyword(String str) {
        for (String keyword : KEYWORDS) {
            if (keyword.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否为运算符
    private boolean isOperator(String str) {
        for (String op : OPERATORS) {
            if (op.equals(str)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否为分隔符
    private boolean isDelimiter(String str) {
        for (String delim : DELIMITERS) {
            if (delim.equals(str)) {
                return true;
            }
        }
        return false;
    }

    // 跳过空白字符
    private void skipWhitespace() {
        while (currentPos < input.length()) {
            char c = input.charAt(currentPos);
            if (c == ' ' || c == '\t') {
                currentPos++;
                currentColumn++;
            } else if (c == '\n') {
                currentPos++;
                currentLine++;
                currentColumn = 1;
            } else {
                break;
            }
        }
    }

    // 读取下一个Token
    public Token nextToken() {
        if (currentPos >= input.length()) {
            return null; // 输入结束
        }

        skipWhitespace();

        if (currentPos >= input.length()) {
            return null;
        }

        char currentChar = input.charAt(currentPos);
        int startLine = currentLine;
        int startColumn = currentColumn;

        // 处理注释
        if (currentChar == '-' && currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '-') {
            return processComment(startLine, startColumn);
        }

        // 处理多行注释
        if (currentChar == '/' && currentPos + 1 < input.length() && input.charAt(currentPos + 1) == '*') {
            return processMultiLineComment(startLine, startColumn);
        }

        // 处理字符串常量
        if (currentChar == '\'') {
            return processStringConstant(startLine, startColumn);
        }

        // 处理数字常量
        if (Character.isDigit(currentChar)) {
            return processNumberConstant(startLine, startColumn);
        }

        // 处理标识符和关键字
        if (Character.isLetter(currentChar) || currentChar == '_') {
            return processIdentifierOrKeyword(startLine, startColumn);
        }

        // 处理运算符
        if (isOperator(Character.toString(currentChar))) {
            return processOperator(startLine, startColumn);
        }

        // 处理分隔符
        if (isDelimiter(Character.toString(currentChar))) {
            return processDelimiter(startLine, startColumn);
        }

        // 无法识别的字符
        String errorValue = Character.toString(currentChar);
        currentPos++;
        currentColumn++;
        return new Token(Token.TokenType.ERROR, errorValue, startLine, startColumn);
    }

    // 处理注释
    private Token processComment(int startLine, int startColumn) {
        StringBuilder comment = new StringBuilder();
        comment.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;
        comment.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;

        while (currentPos < input.length() && input.charAt(currentPos) != '\n') {
            comment.append(input.charAt(currentPos));
            currentPos++;
            currentColumn++;
        }

        return new Token(Token.TokenType.COMMENT, comment.toString(), startLine, startColumn);
    }

    // 处理多行注释
    private Token processMultiLineComment(int startLine, int startColumn) {
        StringBuilder comment = new StringBuilder();
        comment.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;
        comment.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;

        while (currentPos < input.length()) {
            if (currentPos + 1 < input.length() &&
                    input.charAt(currentPos) == '*' &&
                    input.charAt(currentPos + 1) == '/') {
                comment.append("*/");
                currentPos += 2;
                currentColumn += 2;
                break;
            }

            if (input.charAt(currentPos) == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }

            comment.append(input.charAt(currentPos));
            currentPos++;
        }

        return new Token(Token.TokenType.COMMENT, comment.toString(), startLine, startColumn);
    }

    // 处理字符串常量
    private Token processStringConstant(int startLine, int startColumn) {
        StringBuilder str = new StringBuilder();
        str.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;

        boolean escaped = false;
        boolean closed = false;

        while (currentPos < input.length()) {
            char c = input.charAt(currentPos);
            str.append(c);
            currentPos++;
            currentColumn++;

            if (c == '\'' && !escaped) {
                closed = true;
                break;
            }

            escaped = (c == '\\' && !escaped);

            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            }
        }

        if (!closed) {
            return new Token(Token.TokenType.ERROR, str.toString(), startLine, startColumn);
        }

        return new Token(Token.TokenType.CONSTANT, str.toString(), startLine, startColumn);
    }

    // 处理数字常量
    private Token processNumberConstant(int startLine, int startColumn) {
        StringBuilder number = new StringBuilder();
        boolean hasDecimal = false;

        while (currentPos < input.length()) {
            char c = input.charAt(currentPos);
            if (Character.isDigit(c)) {
                number.append(c);
                currentPos++;
                currentColumn++;
            } else if (c == '.' && !hasDecimal) {
                number.append(c);
                currentPos++;
                currentColumn++;
                hasDecimal = true;
            } else {
                break;
            }
        }

        return new Token(Token.TokenType.CONSTANT, number.toString(), startLine, startColumn);
    }

    // 处理标识符或关键字
    private Token processIdentifierOrKeyword(int startLine, int startColumn) {
        StringBuilder identifier = new StringBuilder();

        while (currentPos < input.length()) {
            char c = input.charAt(currentPos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                identifier.append(c);
                currentPos++;
                currentColumn++;
            } else {
                break;
            }
        }

        String id = identifier.toString();
        if (isKeyword(id)) {
            return new Token(Token.TokenType.KEYWORD, id, startLine, startColumn);
        } else {
            return new Token(Token.TokenType.IDENTIFIER, id, startLine, startColumn);
        }
    }

    // 处理运算符
    private Token processOperator(int startLine, int startColumn) {
        StringBuilder operator = new StringBuilder();
        operator.append(input.charAt(currentPos));
        currentPos++;
        currentColumn++;

        // 检查是否为多字符运算符
        if (currentPos < input.length()) {
            char nextChar = input.charAt(currentPos);
            String potentialOp = operator.toString() + nextChar;
            if (isOperator(potentialOp)) {
                operator.append(nextChar);
                currentPos++;
                currentColumn++;
            }
        }

        return new Token(Token.TokenType.OPERATOR, operator.toString(), startLine, startColumn);
    }

    // 处理分隔符
    private Token processDelimiter(int startLine, int startColumn) {
        String delim = Character.toString(input.charAt(currentPos));
        currentPos++;
        currentColumn++;
        return new Token(Token.TokenType.DELIMITER, delim, startLine, startColumn);
    }

    // 获取所有Token
    public List<Token> getAllTokens() {
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = nextToken()) != null) {
            tokens.add(token);
        }
        return tokens;
    }
}