package executor.storageEngine;

import executor.common.Table;
import executor.common.TableSchema;
import storage.page.Page;
import storage.service.StorageService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import common.BPTree.BPTree;
import common.serialize.SerializeUtil;

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
        try {
            // 1. 通过B+树索引查找表名对应的pageId
            Integer pageId = storageService.getTableIndex().search(tableName);
            if (pageId == null) {
                return null; // 未找到表
            }
            // 2. 读取页面并反序列化表结构
            Page page = storageService.readPage(pageId);
            if (page == null) {
                return null;
            }
            byte[] data = page.getData();
            TableSchema schema = SerializeUtil.deserializeTableSchema(data);
            // 3. 返回Table对象（此处可根据你的Table实现调整）
            // return new Table(schema, ...);
            return null; // 暂时返回null，实际应返回Table实例
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TableSchema openTableSchema(String tableName) {
        try {
            // 1. 通过B+树索引查找表名对应的pageId
            Integer pageId = storageService.getTableIndex().search(tableName);
            if (pageId == null) {
                return null; // 未找到表
            }
            // 2. 读取页面并反序列化表结构
            Page page = storageService.readPage(pageId);
            if (page == null) {
                return null;
            }
            byte[] data = page.getData();
            TableSchema schema = SerializeUtil.deserializeTableSchema(data);
            return schema;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void createTable(TableSchema schema) {
        try {
            // 1. 分配一个新页面用于存储表元数据
            int pageId = storageService.allocatePage();
            // 2. 序列化表结构
            byte[] schemaBytes = SerializeUtil.serializeTableSchema(schema);
            byte[] pageData = new byte[Page.PAGE_SIZE];
            System.arraycopy(schemaBytes, 0, pageData, 0, Math.min(schemaBytes.length, Page.PAGE_SIZE));
            // 3. 写入页面
            storageService.writePage(pageId, pageData);
            // 4. 可以将表名和pageId映射关系记录到系统表或内存
            BPTree<String, Integer> tableIndex = storageService.getTableIndex();
            tableIndex.insert(schema.tableName(), pageId);

            storageService.flushAllPages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveTable(String tableName, Table table) {
        
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
