package storage.service;

import storage.buffer.BufferPoolManager;
import storage.buffer.DiskManager;
import storage.page.Page;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.StorageConfig;

public class StorageService {
    private DiskManager diskManager;
    private BufferPoolManager bufferPoolManager;
    private final Logger logger = LoggerFactory.getLogger(StorageService.class);


    public StorageService(String filePathString) {
        try {
            diskManager = new DiskManager(filePathString);
            bufferPoolManager = new BufferPoolManager(StorageConfig.BUFFER_POOL_SIZE, diskManager);
        } catch(IOException e) {
            logger.error("init storage failed", e.getMessage());
        }
    }

    // public StorageService(BufferPoolManager bufferPoolManager) {
    //     this.bufferPoolManager = bufferPoolManager;
    // }

    /**
     * 读取指定 pageId 的页面内容
     * @param pageId 页面ID
     * @return Page对象，若不存在则返回null
     */
    public Page readPage(int pageId) throws IOException {
        return bufferPoolManager.fetchPage(pageId);
    }


    /**
     * 将数据写入指定 pageId 的页面
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
        System.arraycopy(data, 0, page.getData(), 0, Page.PAGE_SIZE);
        page.setDirty(true);
        bufferPoolManager.unpinPage(pageId, true);
        return true;
    }
}