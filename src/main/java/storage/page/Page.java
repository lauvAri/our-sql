package storage.page;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Page {
    public static final int PAGE_SIZE = 4096; // 页大小定义为4KB
    private final ByteBuffer data; // 使用ByteBuffer可以方便地读写基本类型
    private int pageId;
    private int pinCount;
    private boolean isDirty;

    public Page() {
        this.data = ByteBuffer.allocate(PAGE_SIZE);
        this.pageId = -1; // -1 表示无效或空闲
        this.pinCount = 0;
        this.isDirty = false;
    }

    public synchronized void pin() {
        this.pinCount++;
    }

    public synchronized void unpin() {
        if (this.pinCount > 0) {
            this.pinCount--;
        }
    }

    // --- Getters and Setters ---
    public int getPageId() { return pageId; }
    public void setPageId(int pageId) { this.pageId = pageId; }
    public int getPinCount() { return pinCount; }
    public boolean isDirty() { return isDirty; }
    public void setDirty(boolean dirty) { isDirty = dirty; }
    public byte[] getData() { return data.array(); }

    // --- Helper methods to read/write content within the page ---
    public void writeString(int offset, String value) {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        data.position(offset);
        data.putInt(stringBytes.length); // 先写入字符串长度
        data.put(stringBytes);           // 再写入字符串内容
        this.isDirty = true;
    }

    public String readString(int offset) {
        data.position(offset);
        int length = data.getInt();
        byte[] stringBytes = new byte[length];
        data.get(stringBytes);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }
}
