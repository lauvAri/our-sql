package common.serialize;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;

public class SerializeUtil {
    /**
     * 简单序列化 TableSchema:
     * [表名长度][表名][列数][每列: 名长度][名][类型ordinal][长度][是否主键(1字节)]
     */
    public static byte[] serializeTableSchema(TableSchema schema) {
        // 计算需要的字节长度
        byte[] tableNameBytes = schema.tableName().getBytes(StandardCharsets.UTF_8);
        List<ColumnDefinition> columns = schema.columns();
        int totalLen = 4 + tableNameBytes.length + 4; // 表名长度+表名+列数
        for (ColumnDefinition col : columns) {
            byte[] colNameBytes = col.name().getBytes(StandardCharsets.UTF_8);
            totalLen += 4 + colNameBytes.length + 4 + 4 + 1; // 名长度+名+类型+长度+主键
        }
        ByteBuffer buffer = ByteBuffer.allocate(totalLen);
        buffer.putInt(tableNameBytes.length);
        buffer.put(tableNameBytes);
        buffer.putInt(columns.size());
        for (ColumnDefinition col : columns) {
            byte[] colNameBytes = col.name().getBytes(StandardCharsets.UTF_8);
            buffer.putInt(colNameBytes.length);
            buffer.put(colNameBytes);
            buffer.putInt(col.type().ordinal()); // 用ordinal存类型
            buffer.putInt(col.length());
            buffer.put(col.isPrimaryKey() ? (byte)1 : (byte)0);
        }
        return buffer.array();
    }

    /**
     * 反序列化 TableSchema
     */
    public static TableSchema deserializeTableSchema(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        // 读取表名
        int tableNameLen = buffer.getInt();
        byte[] tableNameBytes = new byte[tableNameLen];
        buffer.get(tableNameBytes);
        String tableName = new String(tableNameBytes, StandardCharsets.UTF_8);

        // 读取列数
        int colCount = buffer.getInt();
        List<ColumnDefinition> columns = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            int colNameLen = buffer.getInt();
            byte[] colNameBytes = new byte[colNameLen];
            buffer.get(colNameBytes);
            String colName = new String(colNameBytes, StandardCharsets.UTF_8);
            int typeOrdinal = buffer.getInt();
            ColumnType type = ColumnType.values()[typeOrdinal];
            int length = buffer.getInt();
            boolean isPrimaryKey = buffer.get() == 1;
            columns.add(new ColumnDefinition(colName, type, length, isPrimaryKey));
        }
        return new TableSchema(tableName, columns);
    }
}
