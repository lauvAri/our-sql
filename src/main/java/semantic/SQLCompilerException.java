package semantic;

/**
 * SQL编译器异常类
 */
public class SQLCompilerException extends Exception {
    
    private final String sql;
    
    public SQLCompilerException(String message, String sql) {
        super(message);
        this.sql = sql;
    }
    
    public SQLCompilerException(String message, String sql, Throwable cause) {
        super(message, cause);
        this.sql = sql;
    }
    
    public String getSql() {
        return sql;
    }
    
    @Override
    public String toString() {
        return super.toString() + "\nSQL: " + sql;
    }
}
