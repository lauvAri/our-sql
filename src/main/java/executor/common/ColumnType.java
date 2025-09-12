package executor.common;

/**
 * 列类型
 */
public enum ColumnType {
    INT(Integer.class),
    VARCHAR(String.class),
    BOOLEAN(Boolean.class),
    FLOAT(Double.class),
    TIMESTAMP(java.sql.Timestamp.class);

    private final Class<?> javaType;

    ColumnType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
