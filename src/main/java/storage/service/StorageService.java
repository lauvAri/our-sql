package storage.service;

import storage.buffer.BufferPoolManager;
import storage.buffer.DiskManager;
import storage.page.Page;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.StorageConfig;
import common.BPTree.BPTree;
import common.BPTree.utils.BPTreePersistUtil;

public class StorageService {
    private DiskManager diskManager;
    private BufferPoolManager bufferPoolManager;
    private final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private static final String prePathDB = StorageConfig.prePathDB;
    private static final String prePathIdx = StorageConfig.prePathIdx;

    private String dbFileName;
    private String idxFileName;

    private BPTree<String, Integer> tableIndex;

    public StorageService(String dbFileName, String idxFileName) {
        try {
            this.dbFileName = dbFileName;
            this.idxFileName = idxFileName;
            loadTableIndexFromDisk(idxFileName);
            // fallback
            if (tableIndex == null) {
                this.tableIndex = new BPTree<>();
            }
            diskManager = new DiskManager(dbFileName);
            bufferPoolManager = new BufferPoolManager(StorageConfig.BUFFER_POOL_SIZE, diskManager);
        } catch (IOException e) {
            logger.error("init storage failed", e.getMessage());
        }
    }

    // public StorageService(BufferPoolManager bufferPoolManager) {
    // this.bufferPoolManager = bufferPoolManager;
    // }

    /**
     * 读取指定 pageId 的页面内容
     * 
     * @param pageId 页面ID
     * @return Page对象，若不存在则返回null
     */
    public Page readPage(int pageId) throws IOException {
        return bufferPoolManager.fetchPage(pageId);
    }

    /**
     * 将数据写入指定 pageId 的页面
     * 
     * @param pageId 页面ID
     * @param data   要写入的数据（长度需等于Page.PAGE_SIZE）
     * @return 是否写入成功
     */
    public boolean writePage(int pageId, byte[] data) throws IOException {
        if (data == null || data.length != Page.PAGE_SIZE) {
            logger.error("data is or null or the length of data is less than " + Page.PAGE_SIZE);
            return false;
        }
        Page page = bufferPoolManager.fetchPage(pageId);
        if (page == null) {
            return false;
        }
        logger.info("write data to page: {}", pageId);
        System.arraycopy(data, 0, page.getData(), 0, Page.PAGE_SIZE);
        page.setDirty(true);
        bufferPoolManager.unpinPage(pageId, true);
        return true;
    }

    public int allocatePage() throws IOException {
        return bufferPoolManager.newPage().getPageId();
    }

    public void flushAllPages() throws IOException {
        bufferPoolManager.flushAllPages();

        String fullIdxFileString = prePathIdx + this.idxFileName;
        BPTreePersistUtil.saveToDisk(tableIndex, new File(fullIdxFileString));
    }

    public void close() throws IOException {
        diskManager.close();
    }

    /**
     * 获取索引
     * 
     * @return BPTree
     */
    public BPTree<String, Integer> getTableIndex() {
        return tableIndex;
    }

    private void loadTableIndexFromDisk(String fileName) {
        try {
            File indexFile = new File(prePathIdx + fileName);
            File parent = indexFile.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            if (!indexFile.exists())

            if (indexFile.exists()) {
                this.tableIndex = BPTreePersistUtil.loadFromDisk(indexFile);
            } 
        } catch (Exception e) {
            String msg = "error when load index: " + prePathIdx + fileName;
            logger.error(msg, e);
        }
    }
}