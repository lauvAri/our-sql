package store;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 一个InputStream的装饰器，用于统计对底层流的 read() 方法的调用次数。
 */
public class CountingInputStream extends FilterInputStream {

    private long readCallCount = 0;

    /**
     * 构造函数
     * @param in 要被包装和计数的底层输入流
     */
    public CountingInputStream(InputStream in) {
        super(in);
    }

    /**
     * 重写 read() 方法，每次调用都计数
     */
    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            readCallCount++;
        }
        return result;
    }

    /**
     * 重写 read(byte[]) 方法，每次调用都计数
     */
    @Override
    public int read(byte[] b) throws IOException {
        int result = super.read(b);
        if (result != -1) {
            readCallCount++;
        }
        return result;
    }

    /**
     * 重写 read(byte[], int, int) 方法，每次调用都计数
     * 这是最核心的读取方法，缓冲流和库通常会调用这个版本。
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            readCallCount++;
        }
        return result;
    }

    /**
     * 获取总的读取调用次数
     * @return a long value.
     */
    public long getReadCallCount() {
        return readCallCount;
    }

    /**
     * 重置计数器
     */
    public void resetCount() {
        this.readCallCount = 0;
    }
}
