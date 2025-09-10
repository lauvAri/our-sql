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
        if (root.isLeaf) {
            // 分裂叶子节点作为根的情况
            BPLeafNode<K, V> oldRoot = (BPLeafNode<K, V>) root;

            // 创建新的叶子节点作为右兄弟
            BPLeafNode<K, V> newLeaf = new BPLeafNode<>();

            // 计算分裂点（通常取中间位置）
            int splitPoint = oldRoot.keys.size() / 2;

            // 将后半部分键值对移动到新节点
            newLeaf.keys.addAll(oldRoot.keys.subList(splitPoint, oldRoot.keys.size()));
            newLeaf.values.addAll(oldRoot.values.subList(splitPoint, oldRoot.values.size()));

            // 从原节点移除已移动的键值对
            oldRoot.keys.subList(splitPoint, oldRoot.keys.size()).clear();
            oldRoot.values.subList(splitPoint, oldRoot.values.size()).clear();

            // 维护叶子节点的链表结构
            newLeaf.setNext(oldRoot.next);
            oldRoot.next = newLeaf;

            // 创建新的内部节点作为根
            BPInternalNode<K, V> newRoot = new BPInternalNode<>();

            // 新根的第一个键是右兄弟的第一个键
            newRoot.keys.add(newLeaf.keys.get(0));

            // 将原节点和新节点作为子节点
            newRoot.children.add(oldRoot);
            newRoot.children.add(newLeaf);

            // 更新根节点
            root = newRoot;
        } else {
            // 分裂内部节点作为根的情况
            BPInternalNode<K, V> oldRoot = (BPInternalNode<K, V>) root;

            // 创建新的内部节点作为右兄弟
            BPInternalNode<K, V> newInternal = new BPInternalNode<>();

            // 计算分裂点（中间键将提升到新根）
            int splitPoint = oldRoot.keys.size() / 2;
            K promotedKey = oldRoot.keys.get(splitPoint);

            // 将后半部分键和子节点移动到新节点
            newInternal.keys.addAll(oldRoot.keys.subList(splitPoint + 1, oldRoot.keys.size()));
            newInternal.children.addAll(oldRoot.children.subList(splitPoint + 1, oldRoot.children.size()));

            // 从原节点移除已移动的键和子节点
            oldRoot.keys.subList(splitPoint, oldRoot.keys.size()).clear();
            oldRoot.children.subList(splitPoint + 1, oldRoot.children.size()).clear();

            // 创建新的根节点
            BPInternalNode<K, V> newRoot = new BPInternalNode<>();

            // 提升的键作为新根的键
            newRoot.keys.add(promotedKey);

            // 将原节点和新节点作为子节点
            newRoot.children.add(oldRoot);
            newRoot.children.add(newInternal);

            // 更新根节点
            root = newRoot;
        }
    }
}

