//package executor.common.impl;
//
//import common.BPTree.BPTree;
//import executor.common.CloseableIterator;
//import executor.common.Index;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//
//public class IndexImpl implements Index {
//    private final String name;               // 索引名称
//    private final List<String> columns;      // 索引包含的列名
//    private final BPTree<String, Integer> tree;  // B+树实例
//    private final KeyExtractor keyExtractor; // 键值提取器
//
//    /**
//     * 构造函数
//     * @param name 索引名称
//     * @param columns 索引列名列表
//     */
//    public IndexImpl(String name, List<String> columns) {
//        this.name = Objects.requireNonNull(name, "索引名称不能为空");
//        this.columns = Collections.unmodifiableList(new ArrayList<>(
//                Objects.requireNonNull(columns, "索引列不能为空")));
//        this.tree = new BPTree<>();
//        this.keyExtractor = new KeyExtractor(columns);
//    }
//
//    @Override
//    public List<String> getColumns() {
//        return columns;
//    }
//
//    /**
//     * 根据键值搜索记录
//     * @param key 要搜索的键值
//     * @return 匹配记录的迭代器
//     */
//    @Override
//    public CloseableIterator<Record> search(Object key) {
//        // 将输入键转换为Comparable类型
//        Comparable comparableKey = (Comparable) key;
//        // 从B+树中获取匹配记录，如果不存在则返回空列表
//        List<Record> records = tree.get(comparableKey);
//        return new ListCloseableIterator(records != null ? records : Collections.emptyList());
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    /**
//     * 向索引中添加记录
//     * @param record 要添加的记录
//     */
//    @Override
//    public void onInsert(Record record) {
//        // 从记录中提取键值
//        Comparable key = keyExtractor.extractKey(record);
//        // 将键值对插入B+树
//        tree.insert(key, record);
//    }
//
//    /**
//     * 从索引中删除记录
//     * @param record 要删除的记录
//     */
//    @Override
//    public void onDelete(Record record) {
//        // 从记录中提取键值
//        Comparable key = keyExtractor.extractKey(record);
//        // 从B+树中删除键值对
//        tree.delete(key, record);
//    }
//
//    /**
//     * 范围搜索
//     * @param lower 范围下限
//     * @param upper 范围上限
//     * @return 范围内记录的迭代器
//     */
//    @Override
//    public CloseableIterator<Record> rangeSearch(Object lower, Object upper) {
//        // 将范围边界转换为Comparable类型
//        Comparable lowerBound = (Comparable) lower;
//        Comparable upperBound = (Comparable) upper;
//        // 执行范围查询
//        List<Record> results = tree.rangeSearch(lowerBound, upperBound);
//        return new ListCloseableIterator(results);
//    }
//
//    /**
//     * 键值提取器内部类
//     * 负责从记录中提取索引键
//     */
//    private static class KeyExtractor {
//        private final List<String> columns;  // 索引列名
//
//        public KeyExtractor(List<String> columns) {
//            this.columns = columns;
//        }
//
//        /**
//         * 从记录中提取键值
//         * @param record 数据记录
//         * @return 提取的键值
//         */
//        public Comparable extractKey(Record record) {
//            // 单列索引直接返回对应值
//            if (columns.size() == 1) {
//                return (Comparable) record.get(columns.get(0));
//            }
//
//            // 多列索引返回复合键
//            List<Object> keyParts = new ArrayList<>(columns.size());
//            for (String column : columns) {
//                keyParts.add(record.get(column));
//            }
//            return new CompositeKey(keyParts);
//        }
//    }
//
//    /**
//     * 复合键内部类
//     * 用于处理多列索引的情况
//     */
//    private static class CompositeKey implements Comparable<CompositeKey> {
//        private final List<Object> values;  // 键值组成部分
//
//        public CompositeKey(List<Object> values) {
//            this.values = Collections.unmodifiableList(new ArrayList<>(values));
//        }
//
//        @Override
//        public int compareTo(CompositeKey other) {
//            // 逐个比较键的组成部分
//            for (int i = 0; i < values.size(); i++) {
//                @SuppressWarnings("unchecked")
//                Comparable<Object> thisVal = (Comparable<Object>) values.get(i);
//                Object otherVal = other.values.get(i);
//                int cmp = thisVal.compareTo(otherVal);
//                if (cmp != 0) {
//                    return cmp;
//                }
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o instanceof CompositeKey) {
//                CompositeKey that = (CompositeKey) o;
//                return values.equals(that.values);
//            }
//            return false;
//        }
//
//        @Override
//        public int hashCode() {
//            return values.hashCode();
//        }
//    }
//
//    /**
//     * 列表迭代器实现
//     * 实现了CloseableIterator接口
//     */
//    private static class ListCloseableIterator implements CloseableIterator<Record> {
//        private final Iterator<Record> iterator;
//
//        public ListCloseableIterator(List<Record> records) {
//            this.iterator = records.iterator();
//        }
//
//        @Override
//        public boolean hasNext() {
//            return iterator.hasNext();
//        }
//
//        @Override
//        public Record next() {
//            return iterator.next();
//        }
//
//        @Override
//        public void close() {
//            // 内存迭代器无需特别清理资源
//        }
//    }
//}
