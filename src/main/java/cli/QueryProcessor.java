package cli;

import storage.buffer.BufferPoolManager;
import storage.service.StorageService;
// import storage.table.TableHeap;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class QueryProcessor {
    private  BufferPoolManager bufferPoolManager;
    // 未来这里会有一个 Catalog Manager 来管理所有表
    // private final Catalog catalog;

    private StorageService storageService;

    public QueryProcessor(StorageService storageService) {
        this.storageService = storageService;
    }

    public QueryProcessor(BufferPoolManager bufferPoolManager) {
        this.bufferPoolManager = bufferPoolManager;
    }

    /**
     * 这是整个数据库引擎的核心入口
     * @param sql SQL statement
     * @return a QueryResult object
     */
    public QueryResult process(String sql) {
        String cleanedSql = sql.trim().toLowerCase(Locale.ROOT);
        
        // =================================================================
        // 这个 switch 语句是模拟的。
        // 未来它将被替换为：
        // 1. ANTLR Parser -> AST
        // 2. Planner -> Execution Plan
        // 3. Executor.execute(plan) -> QueryResult
        // =================================================================
        
        try {
            if (cleanedSql.startsWith("create table")) {
                // 模拟创建表
                // new TableHeap(bufferPoolManager);
                return new QueryResult(true, "Table created successfully.");
            } else if (cleanedSql.startsWith("insert into")) {
                // 模拟插入
                return new QueryResult(true, "1 row inserted.");
            } else if (cleanedSql.startsWith("select * from mock_users")) {
                // 模拟一个 SELECT 查询
                List<String> columns = List.of("user_id", "name", "email");
                List<List<Object>> rows = new ArrayList<>();
                rows.add(List.of(1, "Alice", "alice@example.com"));
                rows.add(List.of(2, "Bob", "bob@example.com"));
                return new QueryResult(columns, rows);
            } else if (cleanedSql.equals(".stats")) { // 内部调试命令
                // return new QueryResult(true, bufferPoolManager.getStatistics());
                return null;
            } else {
                 return new QueryResult(false, "Syntax error or unsupported SQL: " + sql);
            }
        } catch (Exception e) {
            return new QueryResult(false, "Execution error: " + e.getMessage());
        }
    }
}