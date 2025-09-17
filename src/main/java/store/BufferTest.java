package store;

import com.fasterxml.jackson.core.type.TypeReference;
import store.CountingInputStream;
import store.Persist;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BufferTest {

    public static void main(String[] args) throws IOException {
        // ... (之前 main 方法中的准备和 Jackson 测试部分可以保留或注释掉) ...

        // --- 准备阶段：创建一个较大的测试文件 ---
        String testFilePath = "large_test_file.json";
        // 确保文件存在，如果不存在就创建它
        if (!new File(testFilePath).exists()) {
            createLargeTestFile(testFilePath, 10000);
        }

        System.out.println("\n--- 新实验：模拟逐字节读取 ---");
        System.out.println("----------------------------------------");

        // 实验三：无缓冲的逐字节读取
        try (FileInputStream fis = new FileInputStream(testFilePath);
             CountingInputStream cis = new CountingInputStream(fis)) {

            System.out.println("3. 开始【无缓冲】逐字节读取...");
            while (cis.read() != -1) {
                // 只是读取，什么也不做
            }
            System.out.println("   完成【无缓冲】逐字节读取。");
            System.out.printf("   >> 底层流 read() 方法被调用了: %d 次%n%n", cis.getReadCallCount());
        }

        // 实验四：有缓冲的逐字节读取
        try (FileInputStream fis = new FileInputStream(testFilePath);
             CountingInputStream cis = new CountingInputStream(fis);
             BufferedInputStream bis = new BufferedInputStream(cis)) {

            System.out.println("4. 开始【有缓冲】逐字节读取...");
            while (bis.read() != -1) {
                // 只是读取，什么也不做
            }
            System.out.println("   完成【有缓冲】逐字节读取。");
            System.out.printf("   >> 底层流 read() 方法被调用了: %d 次%n", cis.getReadCallCount());
        }

        // --- 清理阶段 ---
        new File(testFilePath).delete();
    }

    private static void createLargeTestFile(String filePath, int recordCount) throws IOException {
        List<String> records = new ArrayList<>(recordCount);
        for (int i = 0; i < recordCount; i++) {
            records.add("This is a sample record string, number " + i + " to make the file larger.");
        }
        new Persist().writeObjectToJsonStream(filePath, records);
    }
}