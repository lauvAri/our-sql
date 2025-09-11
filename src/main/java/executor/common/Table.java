package executor.common;

import executor.common.Record;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 表
 */
public interface Table {
    // 数据操作
    CloseableIterator<executor.common.Record> scan();
    void insert(executor.common.Record record);
    void delete(Predicate<Record> condition);
    List<Index> getIndexes();   //获得表上所有索引
    Index getIndex(String indexName);   //获得特定名称的索引
    public void addIndex(Index index);  //添加索引

    // 元数据
    TableSchema getSchema();

    Record getRecord(String key);   //通过主键获取记录
    List<Record> getAllRecords();   //查询所有记录
}

