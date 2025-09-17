package store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 一个实现了LRU（最近最少使用）驱逐策略的、有固定容量的缓存。
 * @param <K> Key
 * @param <V> Value
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(LruCache.class);
    private final int capacity;

    /**
     * 构造函数
     * @param capacity 缓存的最大容量
     */
    public LruCache(int capacity) {
        // initialCapacity: 初始容量
        // loadFactor:    负载因子
        // accessOrder:   true 表示按访问顺序排序（实现LRU的关键），false 表示按插入顺序排序
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * 这是 LinkedHashMap 提供的扩展点，在每次 put/putAll 操作后被调用。
     * 当它返回 true 时，最旧的条目（eldest entry）将被移除。
     * ⭐ 我们在这里记录“替换日志”！
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // 当缓存大小超过设定的容量时，返回 true，触发移除操作
        boolean shouldRemove = size() > capacity;
        if (shouldRemove) {
            logger.info("[Cache Eviction] Cache is full (capacity={}). Evicting least recently used entry: Key='{}'",
                    capacity, eldest.getKey());
        }
        return shouldRemove;
    }
}