package storage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.StorageConfig;
import executor.common.Record;
import executor.common.Table;
import executor.common.TableSchema;
import executor.common.impl.InMemoryTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.buffer.BufferPoolManager;
import storage.buffer.DiskManager;
import storage.page.Page;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

public class MyStorageService {
    private DiskManager dataFileManager;
    private DiskManager indexFileManager;
    private DiskManager schemaFileManager;
    private DiskManager sysCatalogManager;

    private BufferPoolManager dataBufferPoolManager;
    private BufferPoolManager indexBufferPoolManager;
    private BufferPoolManager schemaBufferPoolManager;
    private BufferPoolManager sysCatalogBufferPoolManager;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HashMap<String, TableSchema> schemas;

    private static final Logger logger = LoggerFactory.getLogger(MyStorageService.class);

    public MyStorageService() {
        this.schemas = new HashMap<>();
    }

    public Table openTable(String tableName) {
        try{
            dataFileManager = new DiskManager("/data/"+tableName + ".db");
            dataBufferPoolManager = new BufferPoolManager(100, dataFileManager);
            TableSchema schema = schemas.get(tableName);
            RandomAccessFile raf = new RandomAccessFile(StorageConfig.prePathData + tableName + ".db", "r");
            if (schema != null) {
                Table table = new InMemoryTable(schemas.get(tableName));
                int pageCount = dataFileManager.getPageCount();

                for (int i = 0; i < pageCount; i++) {
                    Page page = dataBufferPoolManager.fetchPage(i);
                    String json = page.readString(page.getStartIndex());
                    page.unpin();
                    logger.info("record: {}", json);
                    Record record = objectMapper.readValue(json, Record.class);
                    table.insert(record);
                }
                return table;
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void createTable(TableSchema schema){
        try{
            schemas.put(schema.tableName(), schema);
            schemaFileManager = new DiskManager("/schema/"+schema.tableName()+".schema");
            schemaBufferPoolManager = new BufferPoolManager(10, schemaFileManager);
            Page page = schemaBufferPoolManager.newPage();
            page.writeString(page.getStartIndex(), objectMapper.writeValueAsString(schema));
            page.unpin();
            schemaBufferPoolManager.flushAllPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void dropTable(String tableName){

    }
    public void saveTable(String tableName, Table table) {
        try {
            RandomAccessFile raf = new RandomAccessFile(StorageConfig.prePathData + tableName + ".db", "rw");
            raf.setLength(0);
            // 初始化DiskManager和BufferPoolManager
            dataFileManager = new DiskManager("/data/" + tableName + ".db");
            dataBufferPoolManager = new BufferPoolManager(5, dataFileManager);

            // 获取所有记录
            List<Record> records = table.getAllRecords();

            // 清空现有数据文件（可选）
            // 这里假设每次保存都会覆盖旧数据
            // 如果需要保留旧数据，可以添加逻辑来处理更新和插入

            // 遍历记录，逐个写入
            for (Record record : records) {
                // 序列化记录为JSON字符串
                String json = objectMapper.writeValueAsString(record);
                logger.info("json: {}", json);

                // 分配新页
                Page page = dataBufferPoolManager.newPage();

                // 写入数据到页
                page.writeString(page.getStartIndex(), json);
                page.unpin();
            }

            // 刷新所有页到磁盘
            dataBufferPoolManager.flushAllPages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createIndex(String tableName, String indexName, List<String> columns, boolean unique){

    }
    public void dropIndex(String tableName, String indexName){

    }
}
