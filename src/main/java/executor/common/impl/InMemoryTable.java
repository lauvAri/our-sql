package executor.common.impl;

import executor.common.CloseableIterator;
import executor.common.Index;
import executor.common.Table;
import executor.common.TableSchema;
import executor.common.Record;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Table接口实现
 */
public class InMemoryTable implements Table {
    private final TableSchema schema;
    private final List<Record> records;
    private final Map<String, Index> indexes;

    public InMemoryTable(TableSchema schema) {
        this.schema = Objects.requireNonNull(schema);
        this.records = new ArrayList<>();
        this.indexes = new ConcurrentHashMap<>();
    }

    @Override
    public CloseableIterator<Record> scan() {
        return new CloseableIterator<Record>() {
            private final Iterator<Record> iterator = records.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Record next() {
                return iterator.next();
            }

            @Override
            public void close() {
                // 清理资源，内存实现无需特别处理
            }
        };
    }

    @Override
    public void insert(Record record) {
        Objects.requireNonNull(record);
        if (!schema.validate(record)) {
            throw new IllegalArgumentException("Record doesn't match table schema");
        }

        synchronized (records) {
            records.add(record);
            // 更新所有索引
            indexes.values().forEach(index -> index.onInsert(record));
        }
    }

    @Override
    public void update(Record record, Record newRecord) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(newRecord);

        // 验证新记录是否符合表结构
        if (!schema.validate(newRecord)) {
            throw new IllegalArgumentException("New record doesn't match table schema");
        }

        synchronized (records) {
            // 检查记录是否存在（可选，根据需求决定是否需要）
            if (!records.contains(record)) {
                throw new IllegalArgumentException("Record not found in table");
            }

            // 更新前从索引中移除旧记录
            indexes.values().forEach(index -> index.onDelete(record));

            // 更新记录内容
            boolean removed = records.remove(record);
            if (!removed) {
                throw new IllegalArgumentException("Old record not found in the table");
            }

            // Add the new record
            records.add(newRecord);

            // 注意：这里假设Record是可变的，如果不可变需要先删除再添加
            newRecord = record.updateFrom(newRecord);

            // 将更新后的记录重新加入索引
            Record finalNewRecord = newRecord;
            indexes.values().forEach(index -> index.onInsert(finalNewRecord));
        }
    }

    @Override
    public void delete(Predicate<Record> condition) {
        Objects.requireNonNull(condition);

        synchronized (records) {
            Iterator<Record> iterator = records.iterator();
            while (iterator.hasNext()) {
                Record record = iterator.next();
                if (condition.test(record)) {
                    iterator.remove();
                    // 从所有索引中删除
                    indexes.values().forEach(index -> index.onDelete(record));
                }
            }
        }
    }

    @Override
    public List<Index> getIndexes() {
        return new ArrayList<>(indexes.values());
    }

    @Override
    public Index getIndex(String indexName) {
        return indexes.get(indexName);
    }

    @Override
    public void addIndex(Index index) {
        Objects.requireNonNull(index);
        synchronized (records) {
            if (indexes.containsKey(index.getName())) {
                throw new IllegalArgumentException("Index already exists: " + index.getName());
            }

            // 为现有数据构建索引
            for (Record record : records) {
                index.onInsert(record);
            }

            indexes.put(index.getName(), index);
        }
    }

    @Override
    public TableSchema getSchema() {
        return schema;
    }

    @Override
    public Record getRecord(String key) {
        for (Record record : records) {
            if (key.equals(record.getPrimaryKey())) {
                return record;
            }
        }
        return null;  // or throw an exception if not found
    }

    @Override
    public List<Record> getAllRecords() {
        return new ArrayList<>(records);
    }
}
