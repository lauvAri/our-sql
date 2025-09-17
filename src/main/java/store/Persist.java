package store;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Persist {

    public Persist() {
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = mapper.getFactory();
    private final Logger logger = LoggerFactory.getLogger(Persist.class);


    public ObjectMapper getMapper() {
        return mapper;
    }
    /**
     * 将对象序列化为JSON并写入文件（已添加缓冲）
     * @param filePath 文件路径
     * @param obj      要序列化的对象
     * @param <T>      对象类型
     * @throws IOException IO异常
     */
    public <T> void writeObjectToJsonStream(String filePath, T obj) throws IOException {
        File file = new File(filePath);
        // 确保父目录存在
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // ⭐ 关键改动 1: 使用 try-with-resources 同时管理文件流和缓冲流
        try (OutputStream fileStream = new FileOutputStream(file);
             OutputStream bufferedStream = new BufferedOutputStream(fileStream); // 使用 BufferedOutputStream 包装
             JsonGenerator generator = jsonFactory.createGenerator(bufferedStream, com.fasterxml.jackson.core.JsonEncoding.UTF8)) {

            mapper.writeValue(generator, obj);
            logger.info("已成功序列化对象到: {}", filePath);
        }
    }

    /**
     * 从JSON文件读取并反序列化对象（已添加缓冲）
     * @param filePath      文件路径
     * @param typeReference 对象类型引用
     * @param <T>           对象类型
     * @return 反序列化后的对象
     * @throws IOException IO异常
     */
    public <T> T readObjectFromJsonStream(String filePath, TypeReference<T> typeReference) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null; // 如果文件不存在，直接返回null
        }

        // ⭐ 关键改动 2: 使用 try-with-resources 同时管理文件流和缓冲流
        try (InputStream fileStream = new FileInputStream(file);
             InputStream bufferedStream = new BufferedInputStream(fileStream)) { // 使用 BufferedInputStream 包装

            return mapper.readValue(bufferedStream, typeReference);
        }
    }

    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                logger.info("已成功删除文件: {}", filePath);
            } else {
                logger.warn("删除文件失败: {}", filePath);
            }
        }
    }
}