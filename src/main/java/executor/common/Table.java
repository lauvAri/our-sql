package executor.common;

import executor.common.Record;

import java.util.Iterator;
import java.util.function.Predicate;

public interface Table {
    // 数据操作
    Iterator<executor.common.Record> scan();
    void insert(executor.common.Record record);
    void delete(Predicate<Record> condition);

    // 元数据
    TableSchema getSchema();
}
