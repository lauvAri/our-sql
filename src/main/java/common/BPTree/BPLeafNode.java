package common.BPTree;

import java.util.ArrayList;
import java.util.List;

/**
 * B+树的叶子节点（存储实际数据）
 */
public class BPLeafNode<K extends Comparable<K>, V> extends BPNode<K, V> {
    List<V> values;          // 存储值（与 keys 一一对应）
    private BPLeafNode<K, V> next; // 指向下一个叶子节点（链表结构）

    public BPLeafNode() {
        super(true);
        this.values = new ArrayList<>();
        this.next = null;
    }

    @Override
    public V search(K key) {
        int index = findKeyIndex(key);
        if (index < keys.size() && keys.get(index).equals(key)) {
            return values.get(index);  // 找到
        }
        return null;  // 未找到
    }

    @Override
    public void insert(K key, V value) {
        int index = findKeyIndex(key);
        keys.add(index, key);
        values.add(index, value);
    }

    @Override
    public void delete(K key) {
        int index = findKeyIndex(key);
        if (index < keys.size() && keys.get(index).equals(key)) {
            keys.remove(index);
            values.remove(index);
        }
    }

    // Getter & Setter
    public BPLeafNode<K, V> getNext() {
        return next;
    }
    public void setNext(BPLeafNode<K, V> next) {
        this.next = next;
    }
}

