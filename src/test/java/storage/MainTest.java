//package storage;
//import org.junit.jupiter.api.*;
//import storage.buffer.BufferPoolManager;
//import storage.buffer.DiskManager;
//import storage.page.Page;
//import java.io.File;
//import java.io.IOException;
//import static org.junit.jupiter.api.Assertions.*;
//
//class MainTest {
//    private static final String DB_FILE = "test_database.db";
//    private DiskManager diskManager;
//    private BufferPoolManager bufferPoolManager;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        new File(DB_FILE).delete();
//        diskManager = new DiskManager(DB_FILE);
//        bufferPoolManager = new BufferPoolManager(3, diskManager);
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        bufferPoolManager.flushAllPages();
//        diskManager.close();
//        new File(DB_FILE).delete();
//    }
//
//    @Test
//    void testPageAllocationAndWriting() throws IOException {
//        Page page0 = bufferPoolManager.newPage();
//        page0.writeString(0, "Hello Page 0 你好，页面0");
//        assertEquals("Hello Page 0 你好，页面0", page0.readString(0));
//
//        Page page1 = bufferPoolManager.newPage();
//        page1.writeString(100, "Data for Page 1");
//        assertEquals("Data for Page 1", page1.readString(100));
//
//        Page page2 = bufferPoolManager.newPage();
//        page2.writeString(200, "Content in Page 2");
//        page2.writeString(200, "new Content in Page2");
//        assertEquals("new Content in Page2", page2.readString(200));
//    }
//
//    @Test
//    void testLRUEvictionAndPersistence()  throws IOException {
//        Page page0 = bufferPoolManager.newPage();
//        page0.writeString(0, "Page0");
//        Page page1 = bufferPoolManager.newPage();
//        page1.writeString(0, "Page1");
//        Page page2 = bufferPoolManager.newPage();
//        page2.writeString(0, "Page2");
//        page2.writeString(200, "Page2Content");
//        bufferPoolManager.unpinPage(page0.getPageId(), true);
//        bufferPoolManager.unpinPage(page1.getPageId(), true);
//        bufferPoolManager.unpinPage(page2.getPageId(), true);
//
//        // Should be in cache
//        Page fetchedPage2 = bufferPoolManager.fetchPage(page2.getPageId());
//        assertEquals("Page2Content", fetchedPage2.readString(200));
//        bufferPoolManager.unpinPage(page2.getPageId(), false);
//    }
//
//    @Test
//    void testOverwriteAndReadBack() throws IOException {
//        Page page = bufferPoolManager.newPage();
//        page.writeString(0, "First");
//        assertEquals("First", page.readString(0));
//        page.writeString(0, "Second");
//        assertEquals("Second", page.readString(0));
//        bufferPoolManager.unpinPage(page.getPageId(), true);
//
//        // 再次获取，检查是否为最新内容
//        Page fetched = bufferPoolManager.fetchPage(page.getPageId());
//        assertEquals("Second", fetched.readString(0));
//        bufferPoolManager.unpinPage(page.getPageId(), false);
//    }
//
//    @Test
//    void testEvictAndReloadMultipleTimes() throws IOException {
//        Page page0 = bufferPoolManager.newPage();
//        page0.writeString(0, "Page0");
//        bufferPoolManager.unpinPage(page0.getPageId(), true);
//
//        Page page1 = bufferPoolManager.newPage();
//        page1.writeString(0, "Page1");
//        bufferPoolManager.unpinPage(page1.getPageId(), true);
//
//        Page page2 = bufferPoolManager.newPage();
//        page2.writeString(0, "Page2");
//        bufferPoolManager.unpinPage(page2.getPageId(), true);
//
//        // 触发淘汰
//        Page page3 = bufferPoolManager.newPage();
//        page3.writeString(0, "Page3");
//        bufferPoolManager.unpinPage(page3.getPageId(), true);
//
//        // page0 应被淘汰，重新加载
//        Page fetchedPage0 = bufferPoolManager.fetchPage(page0.getPageId());
//        assertEquals("Page0", fetchedPage0.readString(0));
//        bufferPoolManager.unpinPage(page0.getPageId(), false);
//
//        // page1 也可能被淘汰，测试其内容
//        Page fetchedPage1 = bufferPoolManager.fetchPage(page1.getPageId());
//        assertEquals("Page1", fetchedPage1.readString(0));
//        bufferPoolManager.unpinPage(page1.getPageId(), false);
//    }
//
//    @Test
//    void testUnpinWithoutDirty() throws IOException {
//        Page page = bufferPoolManager.newPage();
//        page.writeString(0, "Test");
//        bufferPoolManager.unpinPage(page.getPageId(), false); // 未标脏
//        Page fetched = bufferPoolManager.fetchPage(page.getPageId());
//        assertEquals("Test", fetched.readString(0));
//        bufferPoolManager.unpinPage(page.getPageId(), false);
//    }
//
//    @Test
//    void testFlushAllPagesPersistsData() throws IOException {
//        Page page = bufferPoolManager.newPage();
//        page.writeString(0, "FlushTest");
//        bufferPoolManager.unpinPage(page.getPageId(), true);
//        bufferPoolManager.flushAllPages();
//        diskManager.close();
//
//        // 重新打开
//        DiskManager newDiskManager = new DiskManager(DB_FILE);
//        BufferPoolManager newBPM = new BufferPoolManager(3, newDiskManager);
//        Page fetched = newBPM.fetchPage(page.getPageId());
//        assertEquals("FlushTest", fetched.readString(0));
//        newBPM.unpinPage(page.getPageId(), false);
//        newDiskManager.close();
//    }
//
//    @Test
//    void testReadEmptyPageReturnsNullOrEmpty() throws IOException {
//        Page page = bufferPoolManager.newPage();
//        assertTrue(page.readString(0) == null || page.readString(0).isEmpty());
//        bufferPoolManager.unpinPage(page.getPageId(), false);
//    }
//
//    @Test
//    void testPersistenceAfterReopen() throws IOException {
//        Page page1 = bufferPoolManager.newPage();
//        page1.writeString(100, "Persisted Page1");
//        bufferPoolManager.unpinPage(page1.getPageId(), true);
//        bufferPoolManager.flushAllPages();
//        diskManager.close();
//
//        // Reopen
//        DiskManager newDiskManager = new DiskManager(DB_FILE);
//        BufferPoolManager newBPM = new BufferPoolManager(3, newDiskManager);
//        Page reFetchedPage1 = newBPM.fetchPage(page1.getPageId());
//        assertEquals("Persisted Page1", reFetchedPage1.readString(100));
//        newBPM.unpinPage(page1.getPageId(), false);
//        newDiskManager.close();
//    }
//}