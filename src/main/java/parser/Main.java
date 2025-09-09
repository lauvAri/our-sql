package parser;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        String sql = "CREATE TABLE S (\n" +
                "    Sno VARCHAR(10) PRIMARY KEY,\n" +
                "    Sname VARCHAR(20) NOT NULL,\n" +
                "    Ssex CHAR(2) CHECK (Ssex IN ('男', '女')),\n" +
                "    Sage INT,\n" +
                "    sdept VARCHAR(30)\n" +
                ");";
        语法分析(sql);
//        语义分析(sql);
    }

    public static void 语法分析(String sql)
    {
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();
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

    public static void 语义分析(String sql)
    {
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();

        // 创建语法分析器
        SQLParser parser = new SQLParser(tokens);
        ASTNode ast = parser.parse();

        if (ast != null) {
            System.out.println("生成的AST:");
            System.out.println(ast.toString());

            System.out.println("\n生成的中间代码(四元式):");
            parser.printQuadruples();
        } else {
            System.out.println("语法分析失败");
        }
    }


}
