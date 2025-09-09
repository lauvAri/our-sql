package storage.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.page.Page;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

public class DiskManager {
    private static final Logger logger = LoggerFactory.getLogger(DiskManager.class);
    private final RandomAccessFile dbFile;
    private final AtomicInteger pageCounter;

    public DiskManager(String dbFilePath) throws IOException {
        File file = new File(dbFilePath);
        this.dbFile = new RandomAccessFile(file, "rw");
        long fileSize = dbFile.length();
        this.pageCounter = new AtomicInteger((int) Math.ceilDiv(fileSize, Page.PAGE_SIZE)); // 设置为向上取整
        logger.info("Opened DB file '{}'. Found {} pages.", dbFilePath, pageCounter.get());
    }

    public synchronized void readPage(int pageId, byte[] pageData) throws IOException {
        if (pageId >= pageCounter.get()) {
            throw new IllegalArgumentException("Page ID " + pageId + " does not exist.");
        }
        long offset = (long) pageId * Page.PAGE_SIZE;
        dbFile.seek(offset);
        dbFile.readFully(pageData);
        logger.trace("Read page {} from disk.", pageId);
    }

    public synchronized void writePage(int pageId, byte[] pageData) throws IOException {
        long offset = (long) pageId * Page.PAGE_SIZE;
        dbFile.seek(offset);
        dbFile.write(pageData);
        logger.trace("Wrote page {} to disk.", pageId);
    }

    public synchronized int allocatePage() {
        // 直接返回当前的页面计数器值作为新页的ID，然后递增计数器
        return pageCounter.getAndIncrement();
    }

    public void close() throws IOException {
        dbFile.close();
    }
}