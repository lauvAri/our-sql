package parser;

import java.util.List;

public class TokenDebugger {
    public static void main(String[] args) {
        String sql = "CREATE TABLE student(id INT, name VARCHAR, age INT);";
        System.out.println("分析SQL: " + sql);
        
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();
        
        System.out.println("\n词法分析结果:");
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("%d: %s\n", i, token);
        }
    }
}
