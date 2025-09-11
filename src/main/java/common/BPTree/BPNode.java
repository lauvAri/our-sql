package common.BPTree;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * B+树的节点基类（抽象类）
 * @param <K> 键类型（如 String 表名）
 * @param <V> 值类型（如 Integer 页号）
 */
public abstract class BPNode<K extends Comparable<K>, V> implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    protected List<K> keys;          // 节点的键（有序）
    protected boolean isLeaf;        // 是否是叶子节点

    public BPNode(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.isLeaf = isLeaf;
    }

    // 查找键的位置（二分查找）
    protected int findKeyIndex(K key) {
        int low = 0, high = keys.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = keys.get(mid).compareTo(key);
            if (cmp == 0) return mid;      // 找到
            else if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return low;  // 返回插入位置
    }

    // 抽象方法（由子类实现）
    public abstract V search(K key);
    public abstract void insert(K key, V value);
    public abstract void delete(K key);
}
