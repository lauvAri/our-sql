package storage.page;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.StorageConfig;

public class Page {
    public static final int PAGE_SIZE = StorageConfig.PAGE_SIZE; // 页大小定义为4KB
    private final ByteBuffer data; // 使用ByteBuffer可以方便地读写基本类型
    private int pageId;
    private int pinCount; // 页面被使用时，pinCount++; 页面使用完后，pinCount--
    private boolean isDirty;
    private int startIndex;
    private List<Integer> startIndexList;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public int getStartIndex() {
        return startIndex;
    }

    private void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public List<Integer> getStartIndexList() {
        return startIndexList;
    }

    public Page() {
        this.data = ByteBuffer.allocate(PAGE_SIZE);
        this.pageId = -1; // -1 表示无效或空闲
        this.pinCount = 0;
        this.isDirty = false;
        this.startIndexList = new ArrayList<>();
        startIndexList.add(0);
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
        int nextStartIndex = data.position();
        startIndexList.add(nextStartIndex);
        setStartIndex(nextStartIndex);
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
