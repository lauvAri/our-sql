package executor.storageEngine;

import executor.common.Table;
import executor.common.TableSchema;
import storage.page.Page;
import storage.service.StorageService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 存储引擎
 */
public class StorageEngineImpl implements StorageEngine {

    private final StorageService storageService;

    public StorageEngineImpl(StorageService storageService) {
        this.storageService = storageService;
    }
    @Override
    public Table openTable(String tableName) {
        return null;
    }

    @Override
    public void createTable(TableSchema schema) {
        try {
            // 1. 分配一个新页面用于存储表元数据
            int pageId = storageService.allocatePage(); // 你需要在StorageService中实现allocatePage方法
            // 2. 序列化表结构
            byte[] pageData = new byte[Page.PAGE_SIZE];
            ByteBuffer buffer = ByteBuffer.wrap(pageData);
            // 简单写入表名
            byte[] nameBytes = schema.tableName().getBytes(StandardCharsets.UTF_8);
            buffer.putInt(nameBytes.length);
            buffer.put(nameBytes);
            // 可以继续写入字段信息等
            // ...
            // 3. 写入页面
            storageService.writePage(pageId, pageData);
            // 4. 可以将表名和pageId映射关系记录到系统表或内存
            
            storageService.flushAllPages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dropTable(String tableName) {

    }

    @Override
    public void createIndex(String tableName, String indexName, List<String> columns, boolean unique) {

    }

    @Override
    public void dropIndex(String tableName, String indexName) {

    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public boolean tableExists(String tableName) {
        return false;
    }
}
