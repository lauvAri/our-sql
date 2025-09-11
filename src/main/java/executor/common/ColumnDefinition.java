package executor.common;

import executor.common.ColumnType;

/**
 * 列定义
 * @param name
 * @param type
 * @param length
 * @param isPrimaryKey
 */
public record ColumnDefinition(
        String name,
        ColumnType type,
        int length,
        boolean isPrimaryKey
) {
    public ColumnDefinition(String name,ColumnType type,int length){
        this(name,type,length,false);
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
}
