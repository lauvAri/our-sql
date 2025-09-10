package common.BPTree;

/**
 * B+树主类（管理索引）
 */
public class BPTree<K extends Comparable<K>, V> {
    public static final int ORDER = 4;  // B+树的阶（每个节点最多 ORDER 个键）
    private BPNode<K, V> root;       // 根节点

    public BPTree() {
        this.root = new BPLeafNode<>();
    }

    // 插入键值对
    public void insert(K key, V value) {
        root.insert(key, value);
        // 如果根节点分裂，需创建新根
        if (root instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) root;
            if (leaf.keys.size() > ORDER) {
                splitRoot();
            }
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) root;
            if (internal.keys.size() > ORDER) {
                splitRoot();
            }
        }
    }

    // 查询
    public V search(K key) {
        return root.search(key);
    }

    // 删除
    public void delete(K key) {
        root.delete(key);
        // 如果根节点只剩一个子节点，降低树高
        if (!root.isLeaf && root.keys.isEmpty()) {
            root = ((BPInternalNode<K, V>) root).children.get(0);
        }
    }

    // 分裂根节点
    private void splitRoot() {
        // 实现根节点分裂（略）
    }
}

