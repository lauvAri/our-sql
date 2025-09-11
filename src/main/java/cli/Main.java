package cli;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.EndOfFileException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.service.StorageService;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        // --- 1. 系统初始化 ---
        // 使用 StorageService 初始化
        StorageService storageService = new StorageService("main.db", "main.idx");
        QueryProcessor queryProcessor = new QueryProcessor(storageService);

        logger.info("Database system started. Welcome!");
        System.out.println("Enter SQL commands, end with a semicolon ';'. Type '.exit' to quit.");

        // --- 2. JLine 终端设置 (提供更好的用户体验) ---
        Terminal terminal = TerminalBuilder.builder().system(true).build();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

        String prompt = "oursql> ";
        String multiLinePrompt = "      -> ";
        StringBuilder sqlBuffer = new StringBuilder();

        // --- 3. REPL (Read-Eval-Print Loop) ---
        while (true) {
            try {
                String currentPrompt = (sqlBuffer.length() == 0) ? prompt : multiLinePrompt;
                String line = lineReader.readLine(currentPrompt);

                line = line.trim();
                if (line.isEmpty()) continue;

                // 处理内部命令
                if (line.equalsIgnoreCase(".exit")) {
                    break;
                }

                sqlBuffer.append(line).append(" ");

                // 如果不是以分号结尾，则继续读取下一行
                if (!line.endsWith(";")) {
                    continue;
                }

                String finalSql = sqlBuffer.toString();
                sqlBuffer.setLength(0); // 清空buffer

                // --- 4. 执行查询并打印结果 ---
                QueryResult result = queryProcessor.process(finalSql);
                printResult(result);

            } catch (UserInterruptException | EndOfFileException e) {
                // 用户按 Ctrl+C 或 Ctrl+D
                break;
            } catch (Exception e) {
                logger.error("An unexpected error occurred in CLI loop.", e);
                System.out.println("Error: " + e.getMessage());
            }
        }

        // --- 5. 系统关闭 ---
        logger.info("Shutting down database system...");
        storageService.flushAllPages();
        storageService.close();
        logger.info("Shutdown complete.");
    }

    /**
     * 格式化并打印查询结果
     * @param result QueryResult object
     */
    private static void printResult(QueryResult result) {
        if (!result.isSuccess()) {
            System.out.println("Error: " + result.getMessage());
            return;
        }

        if (result.isSelect()) {
            // 打印表格
            if(result.getRows().isEmpty()){
                System.out.println("Empty set.");
                return;
            }
            // 简单实现，未来可以用库来美化
            for(String col : result.getColumnNames()){
                System.out.printf("%-20s", col);
            }
            System.out.println("\n------------------------------------------------------------");
            for(List<Object> row : result.getRows()){
                for(Object val : row){
                    System.out.printf("%-20s", val.toString());
                }
                System.out.println();
            }
            System.out.println("\n(" + result.getRows().size() + " rows)");
        } else {
            // 打印 DDL/DML 消息
            System.out.println(result.getMessage());
        }
    }
}