package storage.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.page.Page;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class BufferPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(BufferPoolManager.class);

    private final Page[] pages; // 缓冲池（帧数组）
    private final Map<Integer, Integer> pageTable; // pageId -> frameId
    private final LRUReplacer replacer;
    private final DiskManager diskManager;
    private final ReentrantLock latch = new ReentrantLock(); // 保护共享数据结构

    public BufferPoolManager(int poolSize, DiskManager diskManager) {
        this.pages = new Page[poolSize];
        for (int i = 0; i < poolSize; i++) {
            this.pages[i] = new Page();
        }
        this.pageTable = new HashMap<>();
        this.diskManager = diskManager;
        this.replacer = new LRUReplacer(poolSize);
    }

    public Page fetchPage(int pageId) throws IOException {
        latch.lock();
        try {
            if (pageTable.containsKey(pageId)) {
                int frameId = pageTable.get(pageId);
                Page page = pages[frameId];
                page.pin();
                replacer.pin(frameId);
                logger.info("Cache HIT for page {}. Found in frame {}.", pageId, frameId);
                return page;
            }

            logger.info("Cache MISS for page {}. Loading from disk.", pageId);
            int frameId = findAvailableFrame();
            if (frameId == -1) {
                logger.error("Cannot fetch page {}. Buffer pool is full and all pages are pinned.", pageId);
                return null;
            }

            Page page = pages[frameId];
            // 如果旧页是脏的，写回磁盘
            if (page.isDirty()) {
                diskManager.writePage(page.getPageId(), page.getData());
            }
            // 从页表中移除旧页的映射
            if (page.getPageId() != -1) {
                pageTable.remove(page.getPageId());
            }

            // 从磁盘加载新页
            diskManager.readPage(pageId, page.getData());
            page.setPageId(pageId);
            page.pin();
            pageTable.put(pageId, frameId);
            replacer.pin(frameId);

            return page;
        } finally {
            latch.unlock();
        }
    }

    public boolean unpinPage(int pageId, boolean isDirty) {
        latch.lock();
        try {
            if (!pageTable.containsKey(pageId)) {
                return false;
            }
            int frameId = pageTable.get(pageId);
            Page page = pages[frameId];
            int before = page.getPinCount();
            page.unpin();
            int after = page.getPinCount();
            logger.info("Unpin page {} in frame {}: pinCount {} -> {}", page, frameId, before, after);
            if (isDirty) {
                page.setDirty(true);
            }
            if (page.getPinCount() == 0) {
                replacer.unpin(frameId);
            }
            return true;
        } finally {
            latch.unlock();
        }
    }

    public Page newPage() throws IOException {
        latch.lock();
        try {
            int frameId = findAvailableFrame();
            if (frameId == -1) {
                logger.error("Cannot create new page. Buffer pool is full and all pages are pinned.");
                return null;
            }

            // 先保存旧页的pageId
            int oldPageId = pages[frameId].getPageId();

            // 如果旧页是脏的，写回磁盘
            if (pages[frameId].isDirty() && oldPageId != -1) {
                diskManager.writePage(oldPageId, pages[frameId].getData());
            }
            // // 从页表中移除旧页的映射
            if (oldPageId != -1) {
                pageTable.remove(oldPageId);
            }

            // 分配新页
            int newPageId = diskManager.allocatePage();
            Page newPage = new Page();
            pages[frameId] = newPage;
            // Page newPage = pages[frameId];

            newPage.setPageId(newPageId);
            newPage.setDirty(false);
            // 重置pinCount
            while (newPage.getPinCount() > 0) {
                newPage.unpin();
            }
            newPage.pin();

            pageTable.put(newPageId, frameId);
            replacer.pin(frameId);

            logger.info("Allocated new page {} in frame {}.", newPageId, frameId);
            return newPage;

        } finally {
            latch.unlock();
        }
    }

    public void flushAllPages() throws IOException {
        latch.lock();
        try {
            for (Page page : pages) {
                if (page != null && page.isDirty()) {
                    diskManager.writePage(page.getPageId(), page.getData());
                    page.setDirty(false);
                }
            }
            logger.info("All dirty pages have been flushed to disk.");
        } finally {
            latch.unlock();
        }
    }

    private int findAvailableFrame() {
        // 先找空闲帧
        for (int i = 0; i < pages.length; i++) {
            if (pages[i].getPageId() == -1) {
                return i;
            }
        }
        // 没有空闲帧，使用LRU淘汰
        return replacer.victim().orElse(-1);
    }
}