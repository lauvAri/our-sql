package cli;

import java.util.List;

public class QueryResult {
    private final String message;
    private final List<String> columnNames;
    private final List<List<Object>> rows;
    private final boolean isSuccess;

    // 用于 DDL/DML (e.g., CREATE, INSERT)
    public QueryResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.columnNames = null;
        this.rows = null;
    }

    // 用于 SELECT
    public QueryResult(List<String> columnNames, List<List<Object>> rows) {
        this.isSuccess = true;
        this.message = "Query executed successfully. " + rows.size() + " rows returned.";
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public boolean isSelect() {
        return columnNames != null && rows != null;
    }
    
    // ... Getters for all fields ...
    public String getMessage() { return message; }
    public List<String> getColumnNames() { return columnNames; }
    public List<List<Object>> getRows() { return rows; }
    public boolean isSuccess() { return isSuccess; }

    @Override
    public String toString() {
        return "QueryResult{" +
                "message='" + message + '\'' +
                ", columnNames=" + columnNames +
                ", rows=" + rows +
                ", isSuccess=" + isSuccess +
                '}';
    }
}
