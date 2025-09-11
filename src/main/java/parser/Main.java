package parser;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        // 测试SELECT语句
        System.out.println("=== 测试 SELECT 语句 ===");
        String sql1 = "SELECT * FROM users WHERE age > 18 AND status = 'active' OR role IN ('admin', 'manager');";
        词法分析(sql1);
        语法分析(sql1);
        
        System.out.println("\n=== 测试 CREATE TABLE 语句 ===");
        String sql2 = "CREATE TABLE product (\n" +
                "    product_id INT,\n" +
                "    product_name VARCHAR,\n" +
                "    price FLOAT,\n" +
                "    is_available BOOLEAN,\n" +
                "    launch_date DATE\n" +
                ");";
        词法分析(sql2);
        语法分析(sql2);
        
        System.out.println("\n=== 测试 INSERT 语句 ===");
        String sql3 = "INSERT INTO product (product_id, product_name, price, is_available, launch_date)\n" +
                "VALUES (101, 'Laptop', 999.99, TRUE, '2024-01-15');";
        词法分析(sql3);
        语法分析(sql3);
        
        System.out.println("\n=== 测试 DELETE 语句 ===");
        String sql4 = "DELETE FROM users WHERE id = 1;";
        词法分析(sql4);
        语法分析(sql4);
    }
    public static void 词法分析(String sql)
    {
        SQLLexer lexer = new SQLLexer(sql);
        List<Token> tokens = lexer.getAllTokens();
        System.out.println("词法分析结果:");
        for (Token token : tokens) {
            System.out.println(token);
        }
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


}
