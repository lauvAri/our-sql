package executor.common;

import java.util.List;

/**
 * 索引
 */
public interface Index {
    /**
     * 获取索引包含的列名
     */
    List<String> getColumns();

    /**
     * 根据键值搜索记录
     */
    CloseableIterator<Record> search(Object key);

    /**
     * 获得索引名
     */
    String getName();

    /**
     * 添加索引
     */
    void onInsert(Record record);

    /**
     * 删除索引
     */
    void onDelete(Record record);

    /**
     * 范围搜索（可选实现）
     */
    default CloseableIterator<Record> rangeSearch(Object lower, Object upper) {
        throw new UnsupportedOperationException("Range search not supported");
    }
}
