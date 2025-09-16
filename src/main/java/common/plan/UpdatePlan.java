package common.plan;

import java.util.List;
import java.util.Map;
import executor.expression.Expression;

public class UpdatePlan extends LogicalPlan {
    private final String tableName;
    private final Map<String, Object> setValues;  // 要更新的列和值
    private final Expression filter;              // WHERE条件

    /**
     * 更新计划构造函数
     * @param tableName 表名
     * @param setValues 要设置的列和值的映射
     * @param filter 更新条件表达式
     */
    public UpdatePlan(String tableName, Map<String, Object> setValues, Expression filter) {
        this.tableName = tableName;
        this.setValues = Map.copyOf(setValues);  // 创建不可变副本
        this.filter = filter;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.UPDATE;
    }

    // Getter方法
    public String getTableName() { return tableName; }
    public Map<String, Object> getSetValues() { return setValues; }
    public Expression getFilter() { return filter; }

    @Override
    public String toString() {
        return "UpdatePlan{" +
                "tableName='" + tableName + '\'' +
                ", setValues=" + setValues +
                ", filter=" + filter +
                '}';
    }
}

