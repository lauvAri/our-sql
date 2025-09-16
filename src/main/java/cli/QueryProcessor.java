package cli;

import common.plan.*;
import executor.executionEngine.ExecutionEngine;
import executor.executionEngine.ExecutionResult;
import parser.semantic.ColumnMetadata;
import parser.semantic.EnhancedMockCatalogAdapter;
import parser.semantic.SQLCompiler;
import parser.semantic.SQLCompilerException;
import storage.buffer.BufferPoolManager;
import storage.service.StorageService;
// import storage.table.TableHeap;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;


public class QueryProcessor {
    private ExecutionEngine engine;
//    private  BufferPoolManager bufferPoolManager;
//    // 未来这里会有一个 Catalog Manager 来管理所有表
//    // private final Catalog catalog;
//
//    private StorageService storageService;
//
//    public QueryProcessor(StorageService storageService) {
//        this.storageService = storageService;
//    }
//
//    public QueryProcessor(BufferPoolManager bufferPoolManager) {
//        this.bufferPoolManager = bufferPoolManager;
//    }


    public QueryProcessor() {
    }

    public void setEngine(ExecutionEngine engine) {
        this.engine = engine;
    }

    /**
     * 这是整个数据库引擎的核心入口
     * @param sql SQL statement
     * @return a QueryResult object
     */
    public QueryResult process(String sql, SQLCompiler compiler) {
        String cleanedSql = sql.trim().toLowerCase(Locale.ROOT);
        
        // =================================================================
        // 这个 switch 语句是模拟的。
        // 未来它将被替换为：
        // 1. ANTLR Parser -> AST
        // 2. Planner -> Execution Plan
        // 3. Executor.execute(plan) -> QueryResult
        // =================================================================

//        try {
//            if (cleanedSql.startsWith("create table")) {
//                // 模拟创建表
//                // new TableHeap(bufferPoolManager);
//                return new QueryResult(true, "Table created successfully.");
//            } else if (cleanedSql.startsWith("insert into")) {
//                // 模拟插入
//                return new QueryResult(true, "1 row inserted.");
//            } else if (cleanedSql.startsWith("select * from mock_users")) {
//                // 模拟一个 SELECT 查询
//                List<String> columns = List.of("user_id", "name", "email");
//                List<List<Object>> rows = new ArrayList<>();
//                rows.add(List.of(1, "Alice", "alice@example.com"));
//                rows.add(List.of(2, "Bob", "bob@example.com"));
//                return new QueryResult(columns, rows);
//            } else if (cleanedSql.equals(".stats")) { // 内部调试命令
//                // return new QueryResult(true, bufferPoolManager.getStatistics());
//                return null;
//            } else {
//                 return new QueryResult(false, "Syntax error or unsupported SQL: " + sql);
//            }
//        } catch (Exception e) {
//            return new QueryResult(false, "Execution error: " + e.getMessage());
//        }
//        // 使用增强的模拟目录适配器
//        EnhancedMockCatalogAdapter mockCatalog = new EnhancedMockCatalogAdapter();
//        SQLCompiler compiler = new SQLCompiler(mockCatalog);
        try {
            // 使用通用编译接口
            LogicalPlan plan = compiler.compile(sql);

            System.out.println("✅ 编译成功!");
            System.out.println("   操作类型: " + plan.getOperatorType());

            // 根据类型显示详细信息
            switch (plan.getOperatorType()) {
                case CREATE_TABLE:
                    CreateTablePlan createPlan = (CreateTablePlan) plan;
                    System.out.println("   表名: " + createPlan.getTableName());
                    System.out.println("   列数: " + createPlan.getColumns().size());

                    // 将CreateTablePlan的列定义转换为ColumnMetadata
                    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                    for (var column : createPlan.getColumns()) {
                        String name = column.getName();
                        String dataType = column.getType();
                        int size = column.getLength() > 0 ? column.getLength() : getDefaultColumnSize(dataType);
                        columnMetadataList.add(new ColumnMetadata(name, dataType, false, column.isPrimaryKey(), size));
                    }


                    engine.execute(plan);
                    return new QueryResult(true, "Table created successfully.");

                case SELECT:
                    SelectPlan selectPlan = (SelectPlan) plan;
                    System.out.println("   查询表: " + selectPlan.getTableName());
                    System.out.println("   选择列: " + selectPlan.getColumns());
                    ExecutionResult result = engine.execute(selectPlan);
                    return new QueryResult(selectPlan.getColumns(), getRecords(result, selectPlan.getColumns()));
                case INSERT:
                    InsertPlan insertPlan = (InsertPlan) plan;
                    System.out.println("   插入表: " + insertPlan.getTableName());
                    System.out.println("   插入值: " + insertPlan.getValues());
                    engine.execute(insertPlan);
                    return new QueryResult(true, "Insert successfully.");

                case DELETE:
                    DeletePlan deletePlan = (DeletePlan) plan;
                    System.out.println("   删除表: " + deletePlan.getTableName());
                    if (deletePlan.getFilter() != null) {
                        System.out.println("   删除条件: " + deletePlan.getFilter());
                    }
                    engine.execute(deletePlan);
                    return new QueryResult(true, "Delete successfully.");

                case UPDATE:
                    UpdatePlan updatePlan = (UpdatePlan) plan;
                    System.out.println("   更新表: " + updatePlan.getTableName());
                    System.out.println("   更新值: " + updatePlan.getSetValues());
                    if (updatePlan.getFilter() != null) {
                        System.out.println("   更新条件: " + updatePlan.getFilter());
                    }
                    result = engine.execute(updatePlan);
                    return new QueryResult(true, result.getData().toString());

                case CREATE_INDEX:
                case DROP_INDEX:
                    System.out.println("   索引操作（暂不详细展示）");
                    break;
            }

        } catch (SQLCompilerException e) {
            System.out.println("❌ 编译失败: " + e.getMessage());

            // 添加更深入的错误分析
            System.out.println("🔍 深度错误分析:");
            try {
                // 1. 测试词法分析
                parser.SQLLexer lexer = new parser.SQLLexer(sql);
                System.out.println("   ✅ 词法分析成功");

                // 2. 测试语法分析
                parser.SQLParser parser = new parser.SQLParser(lexer.getAllTokens());
                parser.ASTNode ast = parser.parse();
                if (ast != null) {
                    System.out.println("   ✅ 语法分析成功 - AST类型: " + ast.getClass().getSimpleName());

                    // 3. 测试语义分析器
                    parser.semantic.EnhancedSemanticAnalyzer analyzer = compiler.getSemanticAnalyzer();
                    parser.semantic.AnalysisResult result = analyzer.analyzeSemantics(ast);

                    if (result.isSuccess()) {
                        System.out.println("   ✅ 语义分析成功");
                    } else {
                        System.out.println("   ❌ 语义分析失败");
                        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                            System.out.println("   详细错误: " + result.getErrors());
                        } else {
                            System.out.println("   错误信息为空，但分析失败");
                        }
                        System.out.println("   完整错误报告:");
                        System.out.println(result.getFormattedResult());
                    }
                } else {
                    System.out.println("   ❌ 语法分析失败 - AST为null");
                }
            } catch (Exception ex) {
                return new QueryResult(false, "Execution error: " + e.getMessage());
            }
        }
        return new QueryResult(false, "Execution complete.");
    }

    /**
     * 重复字符串
     */
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 获取数据类型的默认大小
     */
    private static int getDefaultColumnSize(String dataType) {
        if (dataType.equalsIgnoreCase("INT")) {
            return 4;
        } else if (dataType.toUpperCase().startsWith("VARCHAR")) {
            // 从VARCHAR(n)中提取n，如果没有则返回默认值
            if (dataType.contains("(") && dataType.contains(")")) {
                try {
                    String sizeStr = dataType.substring(dataType.indexOf("(") + 1, dataType.indexOf(")"));
                    return Integer.parseInt(sizeStr);
                } catch (Exception e) {
                    return 255; // 默认VARCHAR大小
                }
            } else {
                return 255; // 默认VARCHAR大小
            }
        } else {
            return 100; // 其他类型的默认大小
        }
    }

    private static List<List<Object>> getRecords(ExecutionResult result, List<String> columnNames) {
        ArrayList<executor.common.Record> records = (ArrayList<executor.common.Record>)(result.getData());
        List<List<Object>> results = new ArrayList<>();
        for (executor.common.Record record : records) {
            ArrayList<Object> row = new ArrayList<>();
            for (String columnName : columnNames) {
                row.add(record.getValue(columnName));
            }
            results.add(row);
        }
        return results;
    }
}