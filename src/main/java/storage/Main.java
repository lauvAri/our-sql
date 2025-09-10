package storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.buffer.BufferPoolManager;
import storage.buffer.DiskManager;
import storage.page.Page;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DB_FILE = "test_database.db";

    public static void main(String[] args) throws IOException {
        // 清理旧的数据库文件
       new File(DB_FILE).delete();

        logger.info("---------- 1. Initializing Storage System ----------");
        DiskManager diskManager = new DiskManager(DB_FILE);
        // 创建一个容量为3的缓冲池，方便观察LRU替换
        BufferPoolManager bufferPoolManager = new BufferPoolManager(3, diskManager);

        logger.info("\n---------- 2. Testing Page Allocation and Writing ----------");
        Page page0 = bufferPoolManager.newPage();
        page0.writeString(0, "Hello Page 0 你好，页面0");
        logger.info("Created page 0 and wrote content.");

        Page page1 = bufferPoolManager.newPage();
        page1.writeString(100, "Data for Page 1");
        logger.info("Created page 1 and wrote content.");

        Page page2 = bufferPoolManager.newPage();
        page2.writeString(200, "Content in Page 2");
        page2.writeString(200, "new Content in Page2"); // 该页写入新的内容
        logger.info("Created page 2 and wrote content.");

        // 释放页面，使其可以被淘汰
        bufferPoolManager.unpinPage(page0.getPageId(), true); // true表示页面是脏的
        bufferPoolManager.unpinPage(page1.getPageId(), true);
        bufferPoolManager.unpinPage(page2.getPageId(), true);
        logger.info("Unpinned pages 0, 1, 2.");

        logger.info("\n---------- 3. Testing Cache Eviction (LRU) ----------");
        logger.info("Buffer pool is full. Fetching a new page should trigger eviction.");
        // 由于page0是最近最少使用的，它应该被淘汰
        Page page3 = bufferPoolManager.newPage();
        page3.writeString(0, "page3");
        logger.info("Created new page {}. Page 0 should have been evicted and flushed.", page3.getPageId());
        bufferPoolManager.unpinPage(page3.getPageId(), false);

        logger.info("\n---------- 4. Testing Cache Hit and Data Verification ----------");
        logger.info("Fetching page 2, which should be in the cache.");
        Page fetchedPage2 = bufferPoolManager.fetchPage(2);
        logger.info("Content of fetched page 2: '{}'", fetchedPage2.readString(200));
        bufferPoolManager.unpinPage(2, false);

        logger.info("\n---------- 5. Testing Persistence ----------");
        logger.info("Fetching page 0, which was evicted. It must be read from disk.");
        Page fetchedPage0 = bufferPoolManager.fetchPage(0);
        logger.info("Content of fetched page 0 from disk: '{}'", fetchedPage0.readString(0));
        bufferPoolManager.unpinPage(0, false);

        logger.info("\n---------- 6. Flushing all dirty pages and closing ----------");
        bufferPoolManager.flushAllPages();
        diskManager.close();

        logger.info("\n---------- 7. Verifying data persistence after reopening ----------");
        DiskManager newDiskManager = new DiskManager(DB_FILE);
        BufferPoolManager newBPM = new BufferPoolManager(3, newDiskManager);
        Page reFetchedPage1 = newBPM.fetchPage(1);
        logger.info("Re-fetched page 1. Content: '{}'", reFetchedPage1.readString(100));
        newBPM.unpinPage(1, false);
        newDiskManager.close();

        // 清理
        // new File(DB_FILE).delete();
        // logger.info("\nTest finished. Database file cleaned up.");
    }
}