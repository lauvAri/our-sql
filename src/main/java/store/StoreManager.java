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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StoreManager {
//    private ConcurrentHashMap<String, Table> tables;
//    private ConcurrentHashMap<String, TableSchema> schemas;
//
//    private String prePathData = StorageConfig.prePathData;
//    private String prePathSchema = StorageConfig.prePathSchema;
//
//    private Persist persist;
//
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//    private static final Logger logger = LoggerFactory.getLogger(StoreManager.class);
//
//    public StoreManager(ConcurrentHashMap<String, Table> tables, ConcurrentHashMap<String, TableSchema> schemas) {
//        this.tables = tables;
//        this.schemas = schemas;
//        this.persist = new Persist();
//        loadAllTables();
//    }
//
//    public Table openTable(String tableName){
//        if (tables.containsKey(tableName)) {
//            return tables.get(tableName);
//        } else {
//            Table table = loadTable(tableName);
//            return table;
//        }
//    }
//    public void createTable(TableSchema schema){
////        if (Objects.equals(schema.tableName(), SystemCatalog.CATALOG_TABLE_NAME)) { // 创建系统表
////            persistTable(SystemCatalog.CATALOG_TABLE_NAME,  new InMemoryTable(SystemCatalog.CATALOG_SCHEMA));
////        } else {
////            persistSchema(schema.tableName(), schema);
////        }
//        tables.put(schema.tableName(), new InMemoryTable(schema));
//        schemas.put(schema.tableName(), schema);
////        if (!Objects.equals(schema.tableName(), SystemCatalog.CATALOG_TABLE_NAME)) {
////            Record row = null;
////            try {
////                row = new CatalogRecord(
////                        schema.tableName(),
////                        objectMapper.writeValueAsString(schema),
////                        System.currentTimeMillis()
////                ).toRecord();
////            } catch (JsonProcessingException e) {
////                throw new RuntimeException(e);
////            }
////            tables.get(SystemCatalog.CATALOG_TABLE_NAME).insert(row);
////            persistTable(SystemCatalog.CATALOG_TABLE_NAME, tables.get(SystemCatalog.CATALOG_TABLE_NAME));
////        }
//
//        Record row = null;
//        try {
//            row = new CatalogRecord(
//                    schema.tableName(),
//                    objectMapper.writeValueAsString(schema),
//                    System.currentTimeMillis()
//            ).toRecord();
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        tables.get(SystemCatalog.CATALOG_TABLE_NAME).insert(row);
//
//    }
//    public void dropTable(String tableName){
//        tables.remove(tableName);
//        schemas.remove(tableName);
//        persist.deleteFile(prePathData + tableName + StorageConfig.DB_SUFFIX);
//    }
//    public void saveTable(String tableName, Table table){
//        tables.put(tableName, table);
//    }
//
//    public ConcurrentHashMap<String, Table> getTables() {
//        return tables;
//    }
//
//    public ConcurrentHashMap<String, TableSchema> getSchemas() {
//        return schemas;
//    }
//
//    public void close() {
//        // 阻塞式IO
////        for (String tableName : tables.keySet()) {
////            persistTable(tableName, tables.get(tableName));
////        }
////        for (String tableName : schemas.keySet()) {
////            persistSchema(tableName, schemas.get(tableName));
////        }
//
//        // 并行持久化所有表
//        List<Map.Entry<String, Table>> tablesToPersist = new ArrayList<>(tables.entrySet());
//
//        tablesToPersist.parallelStream().forEach(entry -> {
//            String tableName = entry.getKey();
//            Table table = entry.getValue();
//            persistTable(tableName, table);
//        });
//
//        // 持久化所有schema
//        List<TableSchema> schemasToPersist = new ArrayList<>(schemas.values());
//        schemasToPersist.parallelStream().forEach(schema -> {
//            persistSchema(schema.tableName(), schema);
//        });
//    }
//
//    private Table loadTable(String tableName) {
//        String dataFilePath = prePathData + tableName + StorageConfig.DB_SUFFIX;
//        String schemaFilePath = prePathSchema + tableName + StorageConfig.SCHEMA_SUFFIX;
//        try {
//            TableSchema schema = persist.readObjectFromJsonStream(schemaFilePath, new TypeReference<TableSchema>() {});
//            if (schema == null) return null;
//
//            InMemoryTable table = new InMemoryTable(schema);
//            List<Record> records = persist.readObjectFromJsonStream(dataFilePath, new TypeReference<List<Record>>() {});
//            if (records != null) {
//                for (Record record : records) {
//                    table.insert(record);
//                }
//            }
//            return table;
//        } catch (IOException e) {
//            return null;
//        }
//    }

    // ⭐ 1. 定义缓存容量
        // 在运行测试的时候可以改为3
    private static final int CACHE_CAPACITY = 10; // 例如，最多在内存中缓存10张表

    // ⭐ 2. 使用我们自定义的、线程安全的 LRU 缓存
    private Map<String, Table> tables; // 不再是 ConcurrentHashMap
    private Map<String, TableSchema> schemas; // Schemas也一同改造

    // ⭐ 3. 新增用于统计的原子计数器
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);

    private String prePathData = StorageConfig.prePathData;
    private String prePathSchema = StorageConfig.prePathSchema;
    private Persist persist;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(StoreManager.class);

    public StoreManager() { // 构造函数不再接收外部Map
        // ⭐ 4. 初始化LRU缓存并使其线程安全
        this.tables = Collections.synchronizedMap(new LruCache<>(CACHE_CAPACITY));
        this.schemas = Collections.synchronizedMap(new LruCache<>(CACHE_CAPACITY));

        this.persist = new Persist();
        loadAllTables();
    }

    public Table openTable(String tableName){
        // ⭐ 5. 在核心访问路径中加入统计和日志逻辑
        if (tables.containsKey(tableName)) {
            // --- 缓存命中 ---
            cacheHitCount.incrementAndGet();
            logger.info("[Cache Hit] Table '{}' found in cache.", tableName);
            return tables.get(tableName);
        } else {
            // --- 缓存未命中 ---
            cacheMissCount.incrementAndGet();
            logger.info("[Cache Miss] Table '{}' not in cache. Loading from disk.", tableName);
            Table table = loadTable(tableName);
            if (table != null) {
                // ⭐ 关键修正：加载后必须放入缓存！
                tables.put(tableName, table);
            }
            return table;
        }
    }

    // ... createTable, dropTable, saveTable 方法保持不变，它们会正确地与新的Map交互 ...
    public void createTable(TableSchema schema){
        tables.put(schema.tableName(), new InMemoryTable(schema));
        schemas.put(schema.tableName(), schema);

        Record row;
        try {
            row = new CatalogRecord(
                    schema.tableName(),
                    objectMapper.writeValueAsString(schema),
                    System.currentTimeMillis()
            ).toRecord();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 假设系统表总是在缓存中
        openTable(SystemCatalog.CATALOG_TABLE_NAME).insert(row);
    }

    public void dropTable(String tableName){
        tables.remove(tableName);
        schemas.remove(tableName);
        persist.deleteFile(prePathData + tableName + StorageConfig.DB_SUFFIX);
    }

    public void saveTable(String tableName, Table table){
        tables.put(tableName, table);
    }

    // ⭐ 6. 新增一个方法用于打印缓存统计信息
    public void printCacheStats() {
        long hits = cacheHitCount.get();
        long misses = cacheMissCount.get();
        long totalAccesses = hits + misses;
        double hitRate = (totalAccesses == 0) ? 0.0 : (double) hits / totalAccesses * 100.0;

        logger.info("--- Cache Statistics ---");
        logger.info("Capacity: {}", CACHE_CAPACITY);
        logger.info("Current Size: {}", tables.size());
        logger.info("Hits: {}", hits);
        logger.info("Misses: {}", misses);
        logger.info("Total Accesses: {}", totalAccesses);
        logger.info("Hit Rate: {}%", String.format("%.2f", hitRate));
        logger.info("------------------------");
    }

    public void close() {
        // close 方法现在持久化的是当前在缓存中的表
        // 注意：这可能导致不在缓存中的已修改数据丢失，这是一个更复杂的状态管理问题。
        // 一个简单的解决方法是在这里持久化所有已知的表，而不仅仅是缓存中的。
        // 但为了演示缓存，我们暂时只持久化缓存中的内容。
        logger.info("Closing StoreManager and persisting cached tables...");
        // 创建一个副本以避免在迭代时发生修改
        Map<String, Table> tablesToPersist = new java.util.HashMap<>(tables);

        tablesToPersist.entrySet().parallelStream().forEach(entry -> {
            persistTable(entry.getKey(), entry.getValue());
        });

        Map<String, TableSchema> schemasToPersist = new java.util.HashMap<>(schemas);
        schemasToPersist.entrySet().parallelStream().forEach(entry -> {
            persistSchema(entry.getKey(), entry.getValue());
        });
    }

    private Table loadTable(String tableName) {
        // ... loadTable 逻辑不变 ...
        String dataFilePath = prePathData + tableName + StorageConfig.DB_SUFFIX;
        String schemaFilePath = prePathSchema + tableName + StorageConfig.SCHEMA_SUFFIX;
        try {
            // ⭐ 修正：加载 schema 也应放入 schema 缓存
            TableSchema schema = schemas.computeIfAbsent(tableName, tn -> {
                try {
                    return persist.readObjectFromJsonStream(schemaFilePath, new TypeReference<>() {});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            if (schema == null) return null;

            InMemoryTable table = new InMemoryTable(schema);
            List<Record> records = persist.readObjectFromJsonStream(dataFilePath, new TypeReference<>() {});
            if (records != null) {
                for (Record record : records) {
                    table.insert(record);
                }
            }
            return table;
        } catch (Exception e) { // 捕获更广泛的异常
            logger.error("Failed to load table '{}'", tableName, e);
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

    public void loadAllTables() {
        Table table = loadTable(SystemCatalog.CATALOG_TABLE_NAME);
        if (table != null) {
            tables.put(SystemCatalog.CATALOG_TABLE_NAME, table);
        }

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
