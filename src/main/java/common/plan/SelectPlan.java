package common.plan;

import java.util.List;

import executor.common.orderby.OrderByClause;
import executor.expression.Expression;

public class SelectPlan extends LogicalPlan {
    private final String tableName;
    private final List<String> columns;
    private final Expression filter; // WHERE 条件表达式

    /*
    todo limit,orderBy改为final
     */
    private OrderByClause orderBy;
    private int limit;

    /**
     *
     * @param tableName 表名
     * @param columns 列
     * @param filter WHERE条件
     */
    public SelectPlan(String tableName, List<String> columns, Expression filter) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
        this.filter = filter;
        this.limit = -1;
        this.orderBy = null;
    }

    public SelectPlan(String tableName, List<String> columns, Expression filter, int limit) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
        this.filter = filter;
        this.limit = limit;
    }

    public SelectPlan(String tableName, List<String> columns, Expression filter, OrderByClause orderBy) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
        this.filter = filter;
        this.orderBy = orderBy;
        this.limit = -1;
    }

    public SelectPlan(String tableName, List<String> columns, Expression filter, OrderByClause orderBy, int limit) {
        this.tableName = tableName;
        this.columns = List.copyOf(columns);
        this.filter = filter;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.SELECT;
    }

    // Setter 方法
    /*
    todo Setter为测试用 应禁用
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public void setOrderBy(OrderByClause orderBy) {
        this.orderBy = orderBy;
    }

    // Getter 方法
    public String getTableName() { return tableName; }
    public List<String> getColumns() { return columns; }
    public Expression getFilter() { return filter; }
    public int getLimit() { return limit; }
    public OrderByClause getOrderBy() { return orderBy; }
}

