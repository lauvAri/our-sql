package cli;

import executor.common.Table;
import executor.common.TableSchema;
import executor.executionEngine.ExecutionEngine;
import executor.storageEngine.StorageEngine;
import executor.storageEngine.StorageEngineImpl;
import executor.systemCatalog.CatalogManager;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.semantic.CatalogAdapter;
import parser.semantic.EnhancedMockCatalogAdapter;
import parser.semantic.SQLCompiler;
import storage.service.StorageService;
import store.StoreManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        // --- 1. 系统初始化 ---
//        // 使用 StorageService 初始化
//        StorageService storageService = new StorageService("main.db", "main.idx");
        QueryProcessor queryProcessor = new QueryProcessor();
//        ConcurrentHashMap<String, Table> tables = new ConcurrentHashMap<>();
//        ConcurrentHashMap<String, TableSchema> schemas = new ConcurrentHashMap<>();

        StoreManager storeManager = new StoreManager();
        StorageEngine storageEngine = new StorageEngineImpl(storeManager);

        ExecutionEngine executionEngine = new ExecutionEngine(storageEngine);
        queryProcessor.setEngine(executionEngine);

        // 创建数据库目录和适配器
        CatalogManager catalogManager = new CatalogManager(storageEngine);
        CatalogAdapter catalogAdapter = new CatalogAdapter(catalogManager);

        // SQL编译器
        SQLCompiler sqlCompiler = new SQLCompiler(catalogAdapter);


        logger.info("Database system started. Welcome!");
        System.out.println("Enter SQL commands, end with a semicolon ';'. Type '.exit' to quit.");

        // --- 2. JLine 终端设置 (提供更好的用户体验) ---


        Terminal terminal = TerminalBuilder
                .builder()
                .system(true)
                .encoding("UTF-8")
                .build();
        // 管理历史记录
        History history = new DefaultHistory();
        Completer historyCompleter = new StringsCompleter(()->
                StreamSupport.stream(history.spliterator(), false)
                        .map(History.Entry::line)
                        .distinct() // 去重
                        .collect(Collectors.toList())
        );

        String historyFile = ".oursql_history";
        Path historyFilePath = Paths.get(System.getProperty("user.home"), historyFile);

        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(historyCompleter)
                .history(history)
                .variable(LineReader.HISTORY_FILE, historyFilePath)
                .build();
        history.load();
        lineReader.setAutosuggestion(LineReader.SuggestionType.COMPLETER);


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
                if (line.equalsIgnoreCase("clear")) {
                    terminal.puts(InfoCmp.Capability.clear_screen);
                    terminal.flush();
                    continue;
                }

                sqlBuffer.append(line).append(" ");

                // 如果不是以分号结尾，则继续读取下一行
                if (!line.endsWith(";")) {
                    continue;
                }

                String finalSql = sqlBuffer.toString();
                history.add(finalSql); // 添加到历史记录
                sqlBuffer.setLength(0); // 清空buffer

                if (finalSql.trim().equalsIgnoreCase("show tables;")) {
                    finalSql = "select id from sys_catalog;";
                }
                // --- 4. 执行查询并打印结果 ---
                QueryResult result = queryProcessor.process(finalSql, sqlCompiler);
                printResult(result, terminal);

            } catch (UserInterruptException e) {
                // 用户按 Ctrl+C
                System.out.println("操作取消");
            } catch ( EndOfFileException e) {
                //  Ctrl+D
                System.out.println("bye!");
                break;
            } catch (Exception e) {
                logger.error("An unexpected error occurred in CLI loop.", e);
                System.out.println("Error: " + e.getMessage());
            }
        }

        // --- 5. 系统关闭 ---
        logger.info("Shutting down database system...");
        storeManager.close();
        logger.info("Shutdown complete.");
    }

    /**
     * 格式化并打印查询结果
     * @param result QueryResult object
     */
    private static void printResult(QueryResult result, Terminal terminal) {
        if (!result.isSuccess()) {
            // 定义一个红色的样式
            AttributedStyle redStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);

            AttributedStringBuilder asb = new AttributedStringBuilder()
                    .style(redStyle).append("Error: ").append(result.getMessage());
            AttributedString as = asb.toAttributedString();
            System.out.println(as.toAnsi(terminal));
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