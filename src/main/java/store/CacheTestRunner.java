package store;

import executor.common.ColumnDefinition;
import executor.common.ColumnType;
import executor.common.TableSchema;

import java.util.ArrayList;
import java.util.List;

public class CacheTestRunner {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- 缓存功能测试启动 ---");
        StoreManager storeManager = new StoreManager();

        // 模拟创建一些表的元信息 (Schema)
        createTestSchemas(storeManager);

        // --- 阶段 1: 填满缓存 (预期：3次未命中) ---
        System.out.println("\n--- 阶段 1: 顺序访问 users, products, orders 以填满缓存 ---");
        storeManager.openTable("users");
        Thread.sleep(100); // 暂停一下，让日志输出更清晰
        storeManager.openTable("products");
        Thread.sleep(100);
        storeManager.openTable("orders");
        System.out.println("--- 阶段 1 完成 ---");
        storeManager.printCacheStats(); // 打印第一次统计

        // --- 阶段 2: 访问已在缓存中的数据 (预期：1次命中) ---
        System.out.println("\n--- 阶段 2: 再次访问 users 表 ---");
        storeManager.openTable("users"); // users 变为最近最常使用
        System.out.println("--- 阶段 2 完成 ---");
        storeManager.printCacheStats(); // 打印第二次统计

        // --- 阶段 3: 触发缓存驱逐 (预期：1次未命中 + 1次替换日志) ---
        // 当前缓存顺序 (从最近到最旧): [users, orders, products]
        // 访问一个新的表 "invoices" 将导致最旧的 "products" 被驱逐
        System.out.println("\n--- 阶段 3: 访问新表 invoices，触发缓存驱逐 ---");
        storeManager.openTable("invoices");
        System.out.println("--- 阶段 3 完成 ---");
        storeManager.printCacheStats();

        // --- 阶段 4: 验证驱逐是否成功 (预期：1次未命中) ---
        // 再次访问 "products"，因为它已被驱逐，所以应该是缓存未命中
        System.out.println("\n--- 阶段 4: 再次访问已被驱逐的 products 表 ---");
        storeManager.openTable("products");
        System.out.println("--- 阶段 4 完成 ---");
        storeManager.printCacheStats();

        System.out.println("\n--- 测试结束，关闭 StoreManager ---");
        storeManager.close();
    }

    /**
     * 一个辅助方法，用于模拟创建几张表的 Schema 并加入 StoreManager
     * @param storeManager
     */
        private static void createTestSchemas(StoreManager storeManager) {
        System.out.println("初始化：创建 users, products, orders, invoices 表的元信息...");

        // Users Table Schema
        List<ColumnDefinition> userCols = new ArrayList<>();
        userCols.add(new ColumnDefinition("id", ColumnType.INT, 0, true));
        userCols.add(new ColumnDefinition("name", ColumnType.VARCHAR, 1, false));
        storeManager.createTable(new TableSchema("users", userCols));

        // Products Table Schema
        List<ColumnDefinition> productCols = new ArrayList<>();
        productCols.add(new ColumnDefinition("sku", ColumnType.INT, 0, true));
        productCols.add(new ColumnDefinition("description", ColumnType.VARCHAR, 1, false));
        storeManager.createTable(new TableSchema("products", productCols));

        // Orders Table Schema
        List<ColumnDefinition> orderCols = new ArrayList<>();
        orderCols.add(new ColumnDefinition("order_id", ColumnType.INT, 0, true));
        orderCols.add(new ColumnDefinition("user_id", ColumnType.INT, 1, false));
        orderCols.add(new ColumnDefinition("product_sku", ColumnType.INT, 2, false));
        orderCols.add(new ColumnDefinition("quantity", ColumnType.INT, 3, false));
        storeManager.createTable(new TableSchema("orders", orderCols));

        // Invoices Table Schema
        List<ColumnDefinition> invoiceCols = new ArrayList<>();
        invoiceCols.add(new ColumnDefinition("invoice_id", ColumnType.INT, 0, true));
        invoiceCols.add(new ColumnDefinition("order_id", ColumnType.INT, 1, false));
        invoiceCols.add(new ColumnDefinition("amount", ColumnType.FLOAT, 2, false));
        storeManager.createTable(new TableSchema("invoices", invoiceCols));
    }

}
