package executor.common.orderby;

public class OrderByItem {
    private final String column;
    private final boolean ascending; // true=ASC, false=DESC

    public OrderByItem(String column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public String getColumn() {
        return column;
    }

    public boolean isAscending() {
        return ascending;
    }
}
