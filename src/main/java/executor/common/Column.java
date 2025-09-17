package executor.common;

import java.util.Objects;

/**
 * 代表数据表中的一个列（Column）的元信息。
 * 这个类被设计为不可变的（Immutable），以确保线程安全和数据一致性。
 */
public final class Column {

    /**
     * 列的名称
     */
    private final String name;

    /**
     * 列的数据类型
     */
    private final ColumnType type;

    /**
     * 列在表中的顺序索引（从0开始）
     */
    private final int index;

    /**
     * 标记该列是否为主键
     */
    private final boolean isPrimaryKey;

    /**
     * 标记该列是否允许为空
     */
    private final boolean isNotNull;

    /**
     * 构造函数，用于创建一个新的 Column 实例。
     *
     * @param name         列名，不能为空
     * @param type         列的数据类型，不能为空
     * @param index        列的索引位置
     * @param isPrimaryKey 是否为主键
     * @param isNotNull    是否不允许为空
     */
    public Column(String name, ColumnType type, int index, boolean isPrimaryKey, boolean isNotNull) {
        // 添加参数校验，确保对象状态的有效性
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Column type cannot be null.");
        }
        this.name = name;
        this.type = type;
        this.index = index;
        this.isPrimaryKey = isPrimaryKey;
        this.isNotNull = isNotNull;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public ColumnType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    // --- Object Methods (toString, equals, hashCode) ---

    /**
     * 返回一个描述该列的字符串，方便调试和日志记录。
     */
    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", index=" + index +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isNotNull=" + isNotNull +
                '}';
    }

    /**
     * 比较两个 Column 对象是否相等。
     * 当所有字段都相同时，我们认为两个 Column 对象相等。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return index == column.index &&
                isPrimaryKey == column.isPrimaryKey &&
                isNotNull == column.isNotNull &&
                Objects.equals(name, column.name) &&
                type == column.type;
    }

    /**
     * 根据对象的字段生成哈希码。
     * 这是确保对象在哈希表（如 HashMap, HashSet）中能正确工作的关键。
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, type, index, isPrimaryKey, isNotNull);
    }
}