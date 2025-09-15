package executor.executionEngine.func;

import executor.common.orderby.OrderByClause;
import executor.common.orderby.OrderByItem;
import executor.common.Record;

import java.util.Comparator;
import java.util.List;

public class OrderByExecutor {
    public List<Record> sort(List<Record> records, OrderByClause orderBy) {
        if (orderBy == null) {
            return records;
        }
        Comparator<Record> comparator = buildComparator(orderBy);
        records.sort(comparator);
        return records;
    }

    private Comparator<Record> buildComparator(OrderByClause orderBy) {
        return (r1, r2) -> {
            for (OrderByItem item : orderBy.getItems()) {
                String column = item.getColumn();
                boolean isAsc = item.isAscending();

                Object v1 = r1.getValue(column);
                Object v2 = r2.getValue(column);

                int cmp = compare(v1, v2);
                if (cmp != 0) {
                    return isAsc ? cmp : -cmp;
                }
            }
            return 0;
        };
    }

    private int compare(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return ((Comparable) a).compareTo(b);
    }
}
