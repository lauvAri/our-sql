package executor.common;

/**
 * 执行异常
 */
public class ExecutionException extends RuntimeException {
    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public enum ErrorCode {
        TABLE_NOT_FOUND,
        COLUMN_NOT_EXISTS,
        TYPE_MISMATCH,
        UNSUPPORTED_OPERATION
    }
}
