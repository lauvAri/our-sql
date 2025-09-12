package semantic;

/**
 * SQL验证结果
 */
public class ValidationResult {
    
    private final boolean success;
    private final String message;
    
    public ValidationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
