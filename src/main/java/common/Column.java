package common;

// Column 定义（列名 + 类型）
public class Column {
    private final String name;
    private final String type;
    private final int length;
    private final boolean primaryKey;

    public Column(String name, String type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.primaryKey = false;
    }

    public Column(String name, String type, int length, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.primaryKey = primaryKey;
    }

    // Getter 方法
    public String getName() { return name; }
    public String getType() { return type; }
    public int getLength() { return length; }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
