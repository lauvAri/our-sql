package executor.executionEngine;

/**
 * 执行结果
 */

public class ExecutionResult {
    private final boolean success;
    private final String message;
    private final Object data;

    private ExecutionResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ExecutionResult success(Object data) {
        return new ExecutionResult(true, "OK", data);
    }

    public static ExecutionResult failure(String error) {
        return new ExecutionResult(false, error, null);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}