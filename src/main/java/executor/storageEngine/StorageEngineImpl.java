package executor.storageEngine;

import executor.common.Table;
import executor.common.TableSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.buffer.BufferPoolManager;
import storage.page.Page;
import storage.service.MyStorageService;
import storage.service.StorageService;

import java.util.List;

import common.BPTree.BPTree;
import common.serialize.SerializeUtil;
import store.StoreManager;

/**
 * 存储引擎
 */
public class StorageEngineImpl implements StorageEngine {
    private static final Logger logger = LoggerFactory.getLogger(StorageEngineImpl.class);


    @Deprecated
    private StorageService storageService;

    @Deprecated
    private MyStorageService myStorageService;

    private StoreManager storeManager;

    @Deprecated
    public StorageEngineImpl(StorageService storageService) {
        this.storageService = storageService;
    }

    @Deprecated
    public StorageEngineImpl(MyStorageService myStorageService) {
        this.myStorageService = myStorageService;
    }

    public StorageEngineImpl(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    private BufferPoolManager bufferPoolManager;

    @Override
    public Table openTable(String tableName) {
//        try {
//            // 1. 通过B+树索引查找表名对应的pageId
//            Integer pageId = storageService.getTableIndex().search(tableName);
//            if (pageId == null) {
//                return null; // 未找到表
//            }
//            // 2. 读取页面并反序列化表结构
//            Page page = storageService.readPage(pageId);
//            if (page == null) {
//                return null;
//            }
//            byte[] data = page.getData();
//            TableSchema schema = SerializeUtil.deserializeTableSchema(data);
//            // 3. 返回Table对象 - 使用InMemoryTable实现
//            return new InMemoryTable(schema);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }

//        return null;

       // return myStorageService.openTable(tableName);
       return storeManager.openTable(tableName);
    }

    @Override
    public void createTable(TableSchema schema) {
//        try {
//            // 1. 分配一个新页面用于存储表元数据
//            int pageId = storageService.allocatePage();
//            // 2. 序列化表结构
//            byte[] schemaBytes = SerializeUtil.serializeTableSchema(schema);
//            byte[] pageData = new byte[Page.PAGE_SIZE];
//            System.arraycopy(schemaBytes, 0, pageData, 0, Math.min(schemaBytes.length, Page.PAGE_SIZE));
//            // 3. 写入页面
//            storageService.writePage(pageId, pageData);
//            // 4. 可以将表名和pageId映射关系记录到系统表或内存
//            BPTree<String, Integer> tableIndex = storageService.getTableIndex();
//            tableIndex.insert(schema.tableName(), pageId);
//
//            storageService.flushAllPages();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        //myStorageService.createTable(schema);
        storeManager.createTable(schema);
    }

    @Override
    public void saveTable(String tableName, Table table) {
//        try {
//            // 1. 查找表是否已经存在
//            Integer pageId = storageService.getTableIndex().search(tableName);
//
//            // 如果表不存在，需要创建新页面来存储表数据
//            if (pageId == null) {
//                // 为表数据分配新页面
//                pageId = storageService.allocatePage();
//                // 将表名和页面ID添加到索引中
//                storageService.getTableIndex().insert(tableName, pageId);
//            }
//
//            // 2. 获取表中的所有记录
//            List<executor.common.Record> records = table.getAllRecords();
//
//            // 3. 读取页面
//            Page page = storageService.readPage(pageId);
//            if (page == null) {
//                logger.error("Failed to read page for table {}", tableName);
//                return;
//            }
//
////            // 4. 初始化页面头部
////            RecordPageManager.initializePageHeader(page);
//
//            // 5. 将所有记录插入到页面中
//            for (Record record : records) {
//                boolean success = RecordPageManager.insertRecord(page, (executor.common.Record) record);
//                if (!success) {
//                    logger.warn("Failed to insert record into table {} page {}", tableName, pageId);
//                }
//            }
//
//            // 6. 刷新所有页面到磁盘
//            storageService.flushAllPages();
//
//            logger.info("Table {} saved successfully with {} records", tableName, records.size());
//        } catch (Exception e) {
//            logger.error("Error saving table {}", tableName, e);
//            e.printStackTrace();
//        }

        //myStorageService.saveTable(tableName, table);

        storeManager.saveTable(tableName, table);
    }


    @Override
    public void dropTable(String tableName) {
//        try {
//            // 从索引中删除表
//            BPTree<String, Integer> tableIndex = storageService.getTableIndex();
//            Integer pageId = tableIndex.search(tableName);
//            if (pageId != null) {
//                tableIndex.delete(tableName);
//                // 注意：实际实现中还需要回收页面资源
//                storageService.flushAllPages();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        storeManager.dropTable(tableName);
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
