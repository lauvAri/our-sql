package common;

public final class StorageConfig { // final 类不可继承
    public static final int PAGE_SIZE = 4096; // 4KB
    public static final int BUFFER_POOL_SIZE = 1 << 8; // 2 ^ 8


    private StorageConfig() {} // 防止实例化
}
