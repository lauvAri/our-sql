package common.BPTree.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.BPTree.BPTree;

public class BPTreePersistUtil {
    private static final Logger logger = LoggerFactory.getLogger(BPTreePersistUtil.class);

    /**
     * 从磁盘文件中加载B+树对象
     * 
     * @param fileName 要加载的文件名
     * @return 加载成功的B+树对象，如果加载失败则返回null
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>, V> BPTree<K, V> loadFromDisk(File idxFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(idxFile))) {
            // 从文件中读取并反序列化B+树对象
            return (BPTree<K, V>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 记录加载错误日志并返回null
            String msg = "error when load " + idxFile.getAbsolutePath();
            logger.error(msg, e);
            return new BPTree<>(); // 返回一个新分配的空的对象
        }
    }

    public static <K extends Comparable<K>, V> void saveToDisk(
                BPTree<K, V> tree, File file) throws IOException {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(file))) {
                oos.writeObject(tree);
            }
    }
}
