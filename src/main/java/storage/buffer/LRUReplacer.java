// src/main/java/storage/buffer/LRUReplacer.java
package storage.buffer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.StorageConfig;

public class LRUReplacer {
    private final LinkedHashMap<Integer, Boolean> cache;
    private final int capacity;
    private static final Logger logger = LoggerFactory.getLogger(BufferPoolManager.class);

    public LRUReplacer(int capacity) {
        this.capacity = capacity;
        // accessOrder=true 会在访问后将元素移到链表尾部（最近使用）
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true);
        logger.info("set cache with the size of {} KB", capacity * (StorageConfig.PAGE_SIZE >> 10));
    }

    public synchronized Optional<Integer> victim() {
        // logger.info("size of the cache: {}", cache.size());
        // if (cache.isEmpty()) return Optional.empty();
        // Integer victim = cache.keySet().iterator().next();
        // cache.remove(victim); // 移除最老的
        // return Optional.of(victim);
        // // LinkedHashMap的迭代器会从最老（头部）到最新（尾部）
        logger.info("size of the cache: {}", cache.size());
        return cache.keySet().stream().findFirst();
    }

    public synchronized void pin(int frameId) {
        cache.remove(frameId);
    }

    public synchronized void unpin(int frameId) {
        if (cache.size() >= capacity) {
            // This case should ideally not be hit if victim() is called properly
            // but as a safeguard, we could remove the eldest.
        }
        if (!cache.containsKey(frameId)) {
            cache.put(frameId, true);
        }
    }
}