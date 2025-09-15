package executor.systemCatalog;

import executor.common.Record;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 系统目录专用记录实现 (不继承Record的版本)
 * 存储表名、表结构JSON和创建时间
 */
public final class CatalogRecord {
    // 字段名常量
    public static final String TABLE_NAME = "id";  // 改为id作为主键
    public static final String SCHEMA_JSON = "schema_json";
    public static final String CREATED_AT = "created_at";

    // 内部数据存储
    private final Map<String, Object> fields;

    /**
     * 构造方法
     * @param tableName 表名(主键)
     * @param schemaJson 表结构的JSON字符串
     * @param createdAt 创建时间戳
     */
    public CatalogRecord(String tableName, String schemaJson, long createdAt) {
        this.fields = createFieldsMap(tableName, schemaJson, createdAt);
    }

    /**
     * 从现有字段构造
     */
    public CatalogRecord(Map<String, Object> fields) {
        this.fields = new HashMap<>(Objects.requireNonNull(fields));
    }

    private static Map<String, Object> createFieldsMap(String tableName, String schemaJson, long createdAt) {
        Map<String, Object> fields = new HashMap<>(3);
        fields.put(TABLE_NAME, tableName);
        fields.put(SCHEMA_JSON, schemaJson);
        fields.put(CREATED_AT, (int)(createdAt / 1000));  // 转换为int类型的秒时间戳
        return fields;
    }

    // 核心访问方法 ==============================================

    public String getTableName() {
        return getStringField(TABLE_NAME);
    }

    public String getSchemaJson() {
        return getStringField(SCHEMA_JSON);
    }

    public long getCreatedAt() {
        Object value = fields.get(CREATED_AT);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        throw new ClassCastException(
                String.format("字段 %s 类型不匹配 (应为 long, 实际为 %s)",
                        CREATED_AT, value != null ? value.getClass().getSimpleName() : "null")
        );
    }

    // 通用字段访问方法 ==========================================

    public boolean hasField(String fieldName) {
        return fields.containsKey(fieldName);
    }

    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }

    public String getStringField(String fieldName) {
        Object value = fields.get(fieldName);
        return value != null ? value.toString() : null;
    }

    public Map<String, Object> getAllFields() {
        return new HashMap<>(fields);
    }

    // 验证方法 ================================================

    /**
     * 验证记录是否是合法的目录记录
     */
    public void validate() {
        if (!hasField(TABLE_NAME) || getTableName() == null) {
            throw new IllegalStateException("缺少表名字段");
        }
        if (!hasField(SCHEMA_JSON)) {
            throw new IllegalStateException("缺少表结构字段");
        }
    }

    // 转换方法 ================================================

    /**
     * 转换为通用Map表示
     */
    public Map<String, Object> toMap() {
        return new HashMap<>(fields);
    }

    /**
     * 转换为Record对象
     */
    public Record toRecord() {
        return new Record(new HashMap<>(fields));
    }


    /**
     * 从Map创建CatalogRecord
     */
    public static CatalogRecord fromMap(Map<String, Object> map) {
        return new CatalogRecord(map);
    }

    @Override
    public String toString() {
        return String.format("CatalogRecord{tableName=%s, createdAt=%d}",
                getTableName(), getCreatedAt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogRecord that = (CatalogRecord) o;
        return fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }
}
