package executor.expression;

import executor.common.Record;

public interface Expression {
    /**
     * 评估表达式并返回结果
     * @param record 当前处理的记录
     * @return 表达式计算结果
     */
    Object evaluate(Record record);
}
