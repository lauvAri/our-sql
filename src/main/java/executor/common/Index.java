package executor.common;

import java.util.List;

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
     * 范围搜索（可选实现）
     */
    default CloseableIterator<Record> rangeSearch(Object lower, Object upper) {
        throw new UnsupportedOperationException("Range search not supported");
    }
}
