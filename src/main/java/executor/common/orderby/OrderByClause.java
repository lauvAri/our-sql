package executor.common.orderby;

import java.util.ArrayList;
import java.util.List;

public class OrderByClause {
    private List<OrderByItem> items;

    public OrderByClause(List<OrderByItem> items) {
        this.items = items;
    }

    public OrderByClause() {
        this.items = new ArrayList<OrderByItem>();
    }

    public OrderByClause addItem(OrderByItem item) {
        items.add(item);
        return this;
    }

    public OrderByClause addItem(String column, boolean ascending) {
        items.add(new OrderByItem(column, ascending));
        return this;
    }

    public List<OrderByItem> getItems() {
        return items;
    }
}
