package common;

import java.io.File;

public final class StorageConfig { // final 类不可继承
    public static final int PAGE_SIZE = 4096; // 4KB
    public static final int BUFFER_POOL_SIZE = 1 << 8; // 2 ^ 8
    public static final String prePathDB = System.getProperty("user.home") + File.separator + ".oursql" + File.separator; // ~/.oursql/
    public static final String prePathIdx = prePathDB + "idx" + File.separator; // ~/.oursql/idx/
    public static final String prePathSchema = prePathDB + "schema" + File.separator; // ~/.oursql/schema/
    public static final String prePathData = prePathDB + "data" + File.separator; // ~/.oursql/data/

    static { // 静态初始化块，在类被加载时执行一次，确保文件路径存在
        File dbDir = new File(prePathDB);
        File idxDir = new File(prePathIdx);
        File schemaDir = new File(prePathSchema);
        File dataDir = new File(prePathData);

        if (!dbDir.exists()) dbDir.mkdir();
        if (!idxDir.exists()) idxDir.mkdir();
        if (!schemaDir.exists()) schemaDir.mkdir();
        if (!dataDir.exists()) dataDir.mkdir();

    }

    private StorageConfig() {} // 防止实例化
}
