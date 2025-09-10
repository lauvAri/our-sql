package executor.common;

import java.util.Iterator;

/**
 * 实现AutoCloseable的Iterator
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
    @Override
    void close(); // 通常只需声明，不需要默认实现
}
