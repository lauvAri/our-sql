package common.serialize;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;


public class SerializeUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 使用ObjectMapper序列化
     */
    public static byte[] serializeTableSchema(TableSchema schema) {
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bos);
//            oos.writeObject(schema);
//            oos.flush();
//            byte[] data = bos.toByteArray();
//            oos.close();
//            bos.close();
//            return data;
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to serialize TableSchema", e);
//        }
        try {
            byte[] schemaBytes = objectMapper.writeValueAsBytes(schema);
            return schemaBytes;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 使用ObjectMapper反序列化
     */
    public static TableSchema deserializeTableSchema(byte[] data) {
        try {
            TableSchema schema = objectMapper.readValue(data, TableSchema.class);
            return schema;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
