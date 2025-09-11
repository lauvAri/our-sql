package semantic;

/**
 * 四元式中间代码表示
 * 格式: [操作符, 操作数1, 操作数2, 结果]
 */
public class Quadruple {
    private String operator;    // 操作符
    private String operand1;    // 操作数1
    private String operand2;    // 操作数2
    private String result;      // 结果
    
    public Quadruple(String operator, String operand1, String operand2, String result) {
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }
    
    // Getter方法
    public String getOperator() { return operator; }
    public String getOperand1() { return operand1; }
    public String getOperand2() { return operand2; }
    public String getResult() { return result; }
    
    // Setter方法
    public void setOperator(String operator) { this.operator = operator; }
    public void setOperand1(String operand1) { this.operand1 = operand1; }
    public void setOperand2(String operand2) { this.operand2 = operand2; }
    public void setResult(String result) { this.result = result; }
    
    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", 
            operator != null ? operator : "_",
            operand1 != null ? operand1 : "_", 
            operand2 != null ? operand2 : "_",
            result != null ? result : "_");
    }
    
    /**
     * 获取格式化的四元式表示
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(operator != null ? operator : "_");
        sb.append(", ").append(operand1 != null ? operand1 : "_");
        sb.append(", ").append(operand2 != null ? operand2 : "_");
        sb.append(", ").append(result != null ? result : "_");
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * 检查四元式是否为空
     */
    public boolean isEmpty() {
        return operator == null && operand1 == null && operand2 == null && result == null;
    }
}
