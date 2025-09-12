package common;

import java.io.File;

public final class StorageConfig { // final 类不可继承
    public static final int PAGE_SIZE = 4096; // 4KB
    public static final int BUFFER_POOL_SIZE = 1 << 8; // 2 ^ 8
    public static final String prePathDB = System.getProperty("user.home") + File.separator + ".oursql" + File.separator;
    public static final String prePathIdx = prePathDB + "idx" + File.separator;


    private StorageConfig() {} // 防止实例化
}
