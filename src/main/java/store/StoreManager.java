package store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.StorageConfig;
import executor.common.Record;
import executor.common.Table;
import executor.common.TableSchema;
import executor.common.impl.InMemoryTable;
import executor.systemCatalog.CatalogRecord;
import executor.systemCatalog.SystemCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreManager {
    private ConcurrentHashMap<String, Table> tables;
    private ConcurrentHashMap<String, TableSchema> schemas;
    
    private String prePathData = StorageConfig.prePathData;
    private String prePathSchema = StorageConfig.prePathSchema;

    private Persist persist;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(StoreManager.class);

    public StoreManager(ConcurrentHashMap<String, Table> tables, ConcurrentHashMap<String, TableSchema> schemas) {
        this.tables = tables;
        this.schemas = schemas;
        this.persist = new Persist();
        loadAllTables();
    }

    public Table openTable(String tableName){
        if (tables.containsKey(tableName)) {
            return tables.get(tableName);
        } else {
            Table table = loadTable(tableName);
            return table;
        }
    }
    public void createTable(TableSchema schema){
//        if (Objects.equals(schema.tableName(), SystemCatalog.CATALOG_TABLE_NAME)) { // 创建系统表
//            persistTable(SystemCatalog.CATALOG_TABLE_NAME,  new InMemoryTable(SystemCatalog.CATALOG_SCHEMA));
//        } else {
//            persistSchema(schema.tableName(), schema);
//        }
        tables.put(schema.tableName(), new InMemoryTable(schema));
        schemas.put(schema.tableName(), schema);
//        if (!Objects.equals(schema.tableName(), SystemCatalog.CATALOG_TABLE_NAME)) {
//            Record row = null;
//            try {
//                row = new CatalogRecord(
//                        schema.tableName(),
//                        objectMapper.writeValueAsString(schema),
//                        System.currentTimeMillis()
//                ).toRecord();
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//            tables.get(SystemCatalog.CATALOG_TABLE_NAME).insert(row);
//            persistTable(SystemCatalog.CATALOG_TABLE_NAME, tables.get(SystemCatalog.CATALOG_TABLE_NAME));
//        }

        Record row = null;
        try {
            row = new CatalogRecord(
                    schema.tableName(),
                    objectMapper.writeValueAsString(schema),
                    System.currentTimeMillis()
            ).toRecord();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        tables.get(SystemCatalog.CATALOG_TABLE_NAME).insert(row);

    }
    public void dropTable(String tableName){
        tables.remove(tableName);
        schemas.remove(tableName);
        persist.deleteFile(prePathData + tableName + StorageConfig.DB_SUFFIX);
    }
    public void saveTable(String tableName, Table table){
        tables.put(tableName, table);
    }

    public ConcurrentHashMap<String, Table> getTables() {
        return tables;
    }

    public ConcurrentHashMap<String, TableSchema> getSchemas() {
        return schemas;
    }

    public void close() {
        // 阻塞式IO
//        for (String tableName : tables.keySet()) {
//            persistTable(tableName, tables.get(tableName));
//        }
//        for (String tableName : schemas.keySet()) {
//            persistSchema(tableName, schemas.get(tableName));
//        }

        // 并行持久化所有表
        List<Map.Entry<String, Table>> tablesToPersist = new ArrayList<>(tables.entrySet());

        tablesToPersist.parallelStream().forEach(entry -> {
            String tableName = entry.getKey();
            Table table = entry.getValue();
            persistTable(tableName, table);
        });

        // 持久化所有schema
        List<TableSchema> schemasToPersist = new ArrayList<>(schemas.values());
        schemasToPersist.parallelStream().forEach(schema -> {
            persistSchema(schema.tableName(), schema);
        });
    }

    private Table loadTable(String tableName) {
        String dataFilePath = prePathData + tableName + StorageConfig.DB_SUFFIX;
        String schemaFilePath = prePathSchema + tableName + StorageConfig.SCHEMA_SUFFIX;
        try {
            TableSchema schema = persist.readObjectFromJsonStream(schemaFilePath, new TypeReference<TableSchema>() {});
            if (schema == null) return null;

            InMemoryTable table = new InMemoryTable(schema);
            List<Record> records = persist.readObjectFromJsonStream(dataFilePath, new TypeReference<List<Record>>() {});
            if (records != null) {
                for (Record record : records) {
                    table.insert(record);
                }
            }
            return table;
        } catch (IOException e) {
            return null;
        }
    }

    private void persistTable(String tableName, Table table) {
        String filePath = prePathData + tableName + StorageConfig.DB_SUFFIX;
        try {
            persist.writeObjectToJsonStream(filePath, table.getAllRecords());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

     private void persistSchema(String tableName, TableSchema schema) {
        String filePath = prePathSchema + tableName + StorageConfig.SCHEMA_SUFFIX;
        try {
            persist.writeObjectToJsonStream(filePath, schema);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAllTables() {
        Table table = loadTable(SystemCatalog.CATALOG_TABLE_NAME);
//        if (table != null) {
//            List<Record> records = table.getAllRecords();
//            for (Record record : records) {
//                logger.info("find: {} in disk", record.getValue("id")); // 这里目前可以获取到表名
//                if (record.getValue("id") != null &&  !record.getJsonString("id").equals(SystemCatalog.CATALOG_TABLE_NAME)){
//                    TableSchema schema = TableSchema.fromJson(record.getJsonString("schema_json"));
//                    tables.put(record.getJsonString("id"), new InMemoryTable(schema));
//                    schemas.put(record.getJsonString("id"), schema);
//                }
//            }
//        }
        // 多线程并行(parallelStream)读取表
        if (table != null) {
            List<Record> records = table.getAllRecords();
            records.parallelStream().forEach(record -> {
                if (record.getValue("id") != null &&  !record.getJsonString("id").equals(SystemCatalog.CATALOG_TABLE_NAME)){
                    TableSchema schema = TableSchema.fromJson(record.getJsonString("schema_json"));

                    tables.put(record.getJsonString("id"), loadTable(record.getJsonString("id")));
                    schemas.put(record.getJsonString("id"), schema);
                }
            });
        }
//        tables.put(SystemCatalog.CATALOG_TABLE_NAME, table);
    }
}
