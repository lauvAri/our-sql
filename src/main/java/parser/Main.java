package parser;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        String sql = "/* test_sql_program */\n" +
                "SELECT name, age\n" +
                "FROM Students\n" +
                "WHERE age > 20;";

        // 创建词法分析器
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();

        System.out.println("词法分析结果:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        // 创建语法分析器
        SQLParser parser = new SQLParser(tokens);
        ASTNode ast = parser.parse();

        // 输出分析结果
        System.out.println("\n语法分析过程:");
        System.out.println(parser.getOutput());

        if (ast != null) {
            System.out.println("生成的AST:");
            System.out.println(ast.toString());
        } else {
            System.out.println("语法分析失败");
        }
    }
}
