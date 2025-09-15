package store;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;

public class Persist {

    public Persist() {
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = mapper.getFactory();

    private final Logger logger = LoggerFactory.getLogger(Persist.class);

    public <T> void writeObjectToJsonStream(String filePath, T obj) throws IOException {
        try (JsonGenerator generator = jsonFactory.createGenerator(new File(filePath),
                com.fasterxml.jackson.core.JsonEncoding.UTF8)) {

            mapper.writeValue(generator, obj); // ✅ Jackson 自动递归遍历所有字段！

            logger.info("已成功序列化对象到: " + filePath);
        }
    }

    public <T> T readObjectFromJsonStream(String filePath, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(new File(filePath), typeReference);
    }

    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
