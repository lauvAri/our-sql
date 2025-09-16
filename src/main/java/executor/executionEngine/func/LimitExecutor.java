package executor.executionEngine.func;

import executor.common.Record;

import java.util.List;

public class LimitExecutor {
    public List<Record> limit(List<Record> records, int limit) {
        if (limit <= 0 || records.size() <= limit) {
            return records;
        }
        return records.subList(0, limit);
    }
}
