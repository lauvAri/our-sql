package common.BPTree;
import java.util.ArrayList;
import java.util.List;

/**
 * B+树的内部节点（存储索引）
 */
public class BPInternalNode<K extends Comparable<K>, V> extends BPNode<K, V> {
    List<BPNode<K, V>> children;  // 子节点（比 keys 多 1 个）

    public BPInternalNode() {
        super(false);
        this.children = new ArrayList<>();
    }

    @Override
    public V search(K key) {
        int index = findKeyIndex(key);
        return children.get(index).search(key);  // 递归查询子节点
    }

    @Override
    public void insert(K key, V value) {
        int index = findKeyIndex(key);
        BPNode<K, V> child = children.get(index);
        child.insert(key, value);

        // 如果子节点分裂，需处理新键和子节点
        if (child instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) child;
            if (leaf.keys.size() > BPTree.ORDER) {
                splitLeafNode(leaf, index);
            }
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) child;
            if (internal.keys.size() > BPTree.ORDER) {
                splitInternalNode(internal, index);
            }
        }
    }

    // 分裂叶子节点
    private void splitLeafNode(BPLeafNode<K, V> leaf, int index) {
        // 创建新叶子节点
        BPLeafNode<K, V> newLeaf = new BPLeafNode<>();

        // 计算分裂点（取中间偏右的位置）
        int splitPoint = leaf.keys.size() / 2;

        // 将原叶子节点后半部分移到新节点
        newLeaf.keys.addAll(leaf.keys.subList(splitPoint, leaf.keys.size()));
        newLeaf.values.addAll(leaf.values.subList(splitPoint, leaf.values.size()));

        // 移除原叶子节点已移动的数据
        leaf.keys.subList(splitPoint, leaf.keys.size()).clear();
        leaf.values.subList(splitPoint, leaf.values.size()).clear();

        // 维护叶子节点链表
        newLeaf.setNext(leaf.getNext());
        leaf.setNext(newLeaf);

        // 将新节点的第一个key提升到父节点
        K promotedKey = newLeaf.keys.get(0);

        // 在父节点插入新key和子节点
        keys.add(index, promotedKey);
        children.add(index + 1, newLeaf);
    }

    // 分裂内部节点
    private void splitInternalNode(BPInternalNode<K, V> internal, int index) {
        // 创建新内部节点
        BPInternalNode<K, V> newInternal = new BPInternalNode<>();

        // 计算分裂点（取中间位置）
        int splitPoint = internal.keys.size() / 2;
        K promotedKey = internal.keys.get(splitPoint);

        // 将原内部节点后半部分移到新节点
        newInternal.keys.addAll(internal.keys.subList(splitPoint + 1, internal.keys.size()));
        newInternal.children.addAll(internal.children.subList(splitPoint + 1, internal.children.size()));

        // 移除原内部节点已移动的数据
        internal.keys.subList(splitPoint, internal.keys.size()).clear();
        internal.children.subList(splitPoint + 1, internal.children.size()).clear();

        // 在父节点插入新key和子节点
        keys.add(index, promotedKey);
        children.add(index + 1, newInternal);
    }

    @Override
    public void delete(K key) {
        int index = findKeyIndex(key);
        BPNode<K, V> child = children.get(index);
        child.delete(key);

        // 检查子节点是否需要合并或借用
        if (child.keys.size() < (BPTree.ORDER / 2)) {
            // 尝试从左兄弟节点借用
            if (index > 0 && children.get(index - 1).keys.size() > (BPTree.ORDER / 2)) {
                borrowFromLeftSibling(index);
            }
            // 尝试从右兄弟节点借用
            else if (index < children.size() - 1 && children.get(index + 1).keys.size() > (BPTree.ORDER / 2)) {
                borrowFromRightSibling(index);
            }
            // 无法借用，需要合并
            else {
                if (index > 0) {
                    mergeWithLeftSibling(index);
                } else {
                    mergeWithRightSibling(index);
                }
            }
        }
    }

    //左节点借用
    private void borrowFromLeftSibling(int index) {
        BPNode<K, V> child = children.get(index);
        BPNode<K, V> leftSibling = children.get(index - 1);

        if (child instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) child;
            BPLeafNode<K, V> leftLeaf = (BPLeafNode<K, V>) leftSibling;

            // 从左兄弟节点借最后一个键值对
            K borrowedKey = leftLeaf.keys.remove(leftLeaf.keys.size() - 1);
            V borrowedValue = leftLeaf.values.remove(leftLeaf.values.size() - 1);

            // 插入到当前节点
            leaf.keys.add(0, borrowedKey);
            leaf.values.add(0, borrowedValue);

            // 更新父节点的key
            keys.set(index - 1, leaf.keys.get(0));
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) child;
            BPInternalNode<K, V> leftInternal = (BPInternalNode<K, V>) leftSibling;

            // 从左兄弟节点借最后一个key和子节点
            K borrowedKey = leftInternal.keys.remove(leftInternal.keys.size() - 1);
            BPNode<K, V> borrowedChild = leftInternal.children.remove(leftInternal.children.size() - 1);

            // 将父节点的key下移，并借用key提升
            K parentKey = keys.get(index - 1);
            internal.keys.add(0, parentKey);
            internal.children.add(0, borrowedChild);

            // 更新父节点的key
            keys.set(index - 1, borrowedKey);
        }
    }

    //右节点借用
    private void borrowFromRightSibling(int index) {
        BPNode<K, V> child = children.get(index);
        BPNode<K, V> rightSibling = children.get(index + 1);

        if (child instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) child;
            BPLeafNode<K, V> rightLeaf = (BPLeafNode<K, V>) rightSibling;

            // 从右兄弟节点借第一个键值对
            K borrowedKey = rightLeaf.keys.remove(0);
            V borrowedValue = rightLeaf.values.remove(0);

            // 插入到当前节点
            leaf.keys.add(borrowedKey);
            leaf.values.add(borrowedValue);

            // 更新父节点的key
            keys.set(index, rightLeaf.keys.get(0));
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) child;
            BPInternalNode<K, V> rightInternal = (BPInternalNode<K, V>) rightSibling;

            // 从右兄弟节点借第一个key和子节点
            K borrowedKey = rightInternal.keys.remove(0);
            BPNode<K, V> borrowedChild = rightInternal.children.remove(0);

            // 将父节点的key下移，并借用key提升
            K parentKey = keys.get(index);
            internal.keys.add(parentKey);
            internal.children.add(borrowedChild);

            // 更新父节点的key
            keys.set(index, borrowedKey);
        }
    }

    //合并左兄弟
    private void mergeWithLeftSibling(int index) {
        BPNode<K, V> child = children.get(index);
        BPNode<K, V> leftSibling = children.get(index - 1);

        if (child instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) child;
            BPLeafNode<K, V> leftLeaf = (BPLeafNode<K, V>) leftSibling;

            // 合并到左兄弟节点
            leftLeaf.keys.addAll(leaf.keys);
            leftLeaf.values.addAll(leaf.values);
            leftLeaf.setNext(leaf.getNext());

            // 移除父节点的key和子节点
            keys.remove(index - 1);
            children.remove(index);
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) child;
            BPInternalNode<K, V> leftInternal = (BPInternalNode<K, V>) leftSibling;

            // 将父节点的key下移
            K parentKey = keys.get(index - 1);
            leftInternal.keys.add(parentKey);
            leftInternal.keys.addAll(internal.keys);
            leftInternal.children.addAll(internal.children);

            // 移除父节点的key和子节点
            keys.remove(index - 1);
            children.remove(index);
        }
    }

    //合并右兄弟
    private void mergeWithRightSibling(int index) {
        BPNode<K, V> child = children.get(index);
        BPNode<K, V> rightSibling = children.get(index + 1);

        if (child instanceof BPLeafNode) {
            BPLeafNode<K, V> leaf = (BPLeafNode<K, V>) child;
            BPLeafNode<K, V> rightLeaf = (BPLeafNode<K, V>) rightSibling;

            // 合并到当前节点
            leaf.keys.addAll(rightLeaf.keys);
            leaf.values.addAll(rightLeaf.values);
            leaf.setNext(rightLeaf.getNext());

            // 移除父节点的key和子节点
            keys.remove(index);
            children.remove(index + 1);
        } else {
            BPInternalNode<K, V> internal = (BPInternalNode<K, V>) child;
            BPInternalNode<K, V> rightInternal = (BPInternalNode<K, V>) rightSibling;

            // 将父节点的key下移
            K parentKey = keys.get(index);
            internal.keys.add(parentKey);
            internal.keys.addAll(rightInternal.keys);
            internal.children.addAll(rightInternal.children);

            // 移除父节点的key和子节点
            keys.remove(index);
            children.remove(index + 1);
        }
    }

}

