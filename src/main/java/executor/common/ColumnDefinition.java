package executor.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import executor.common.ColumnType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

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
) implements Serializable {
    public ColumnDefinition(String name,ColumnType type,int length){
        this(name,type,length,false);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return length == 0;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
}
