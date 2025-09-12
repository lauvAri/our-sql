package storage;

import common.BPTree.BPTree;

public class BPTreeTest {
    public static void main(String[] args) {
        BPTree<String, Integer> index = new BPTree<>();

        // 插入数据（表名 -> 页号）
        index.insert("users", 1);
        index.insert("orders", 2);
        index.insert("products", 3);

        // 查询
        System.out.println(index.search("users"));   // 输出 1
        System.out.println(index.search("orders"));  // 输出 2

        // 删除
        index.delete("orders");
        System.out.println(index.search("orders"));  // 输出 null
    }
}
