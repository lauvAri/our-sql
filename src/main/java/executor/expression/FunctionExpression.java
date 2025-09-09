package executor.expression;

import executor.common.Record;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 函数表达式
 */
public class FunctionExpression implements Expression {
    private final String functionName;
    private final List<Expression> arguments;

    public FunctionExpression(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public Object evaluate(Record record) {
        List<Object> evaluatedArgs = arguments.stream()
                .map(expr -> expr.evaluate(record))
                .collect(Collectors.toList());

        switch (functionName.toUpperCase()) {
            case "UPPER": return evaluatedArgs.get(0).toString().toUpperCase();
            case "LOWER": return evaluatedArgs.get(0).toString().toLowerCase();
            case "LENGTH": return evaluatedArgs.get(0).toString().length();
            case "SUBSTR":
                String str = evaluatedArgs.get(0).toString();
                int start = (Integer)evaluatedArgs.get(1);
                int length = (Integer)evaluatedArgs.get(2);
                return str.substring(start, Math.min(start + length, str.length()));
            default:
                throw new UnsupportedOperationException("Unknown function: " + functionName);
        }
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}

