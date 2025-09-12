package storage.page;

import executor.common.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理页面和数据库表记录的关系
 * 负责在页面中组织、存储和检索记录
 */
public class RecordPageManager {
    private static final Logger logger = LoggerFactory.getLogger(RecordPageManager.class);

    // 页面头部大小（用于存储元数据）
    private static final int PAGE_HEADER_SIZE = 32;

    // 页面中记录数量的偏移量
    private static final int RECORD_COUNT_OFFSET = 0;

    // 空闲空间起始位置的偏移量
    private static final int FREE_SPACE_OFFSET = 4;

    // 记录目录起始位置的偏移量
    private static final int DIRECTORY_OFFSET = 8;

    /**
     * 在页面中插入一条记录
     *
     * @param page 页面对象
     * @param record 要插入的记录
     * @return 是否插入成功
     */
    public static boolean insertRecord(Page page, Record record) {
        try {
            byte[] pageData = page.getData();
            byte[] recordData = serializeRecord(record);

            // 检查是否有足够空间
            int freeSpaceOffset = getFreeSpaceOffset(pageData);
            int requiredSpace = 4 + recordData.length; // 4字节记录长度 + 记录数据

            if (freeSpaceOffset + requiredSpace > Page.PAGE_SIZE) {
                logger.warn("Not enough space in page {} to insert record", page.getPageId());
                return false;
            }

            // 更新记录目录
            int recordCount = getRecordCount(pageData);
            int directoryEntryOffset = DIRECTORY_OFFSET + (recordCount * 4); // 每个目录项4字节（记录偏移量）

            if (directoryEntryOffset + 4 > PAGE_HEADER_SIZE) {
                logger.error("Page header overflow in page {}", page.getPageId());
                return false;
            }

            // 写入记录偏移量到目录
            ByteBuffer.wrap(pageData).putInt(directoryEntryOffset, freeSpaceOffset);

            // 写入记录长度和记录数据
            ByteBuffer.wrap(pageData).putInt(freeSpaceOffset, recordData.length);
            System.arraycopy(recordData, 0, pageData, freeSpaceOffset + 4, recordData.length);

            // 更新记录数量
            setRecordCount(pageData, recordCount + 1);

            // 更新空闲空间位置
            setFreeSpaceOffset(pageData, freeSpaceOffset + 4 + recordData.length);

            // 标记页面为脏页
            page.setDirty(true);

            logger.debug("Record inserted successfully into page {}", page.getPageId());
            return true;
        } catch (Exception e) {
            logger.error("Error inserting record into page {}", page.getPageId(), e);
            return false;
        }
    }

    /**
     * 从页面中获取所有记录
     *
     * @param page 页面对象
     * @return 记录列表
     */
    public static List<Record> getAllRecords(Page page) {
        List<Record> records = new ArrayList<>();
        try {
            byte[] pageData = page.getData();
            int recordCount = getRecordCount(pageData);

            for (int i = 0; i < recordCount; i++) {
                Record record = getRecordAt(pageData, i);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving records from page {}", page.getPageId(), e);
        }

        return records;
    }

    /**
     * 获取页面中的记录数量
     *
     * @param pageData 页面数据
     * @return 记录数量
     */
    private static int getRecordCount(byte[] pageData) {
        return ByteBuffer.wrap(pageData).getInt(RECORD_COUNT_OFFSET);
    }

    /**
     * 设置页面中的记录数量
     *
     * @param pageData 页面数据
     * @param count 记录数量
     */
    private static void setRecordCount(byte[] pageData, int count) {
        ByteBuffer.wrap(pageData).putInt(RECORD_COUNT_OFFSET, count);
    }

    /**
     * 获取页面中空闲空间的起始位置
     *
     * @param pageData 页面数据
     * @return 空闲空间起始位置
     */
    private static int getFreeSpaceOffset(byte[] pageData) {
        // 如果是新页面，初始化空闲空间位置
        int freeSpaceOffset = ByteBuffer.wrap(pageData).getInt(FREE_SPACE_OFFSET);
        if (freeSpaceOffset == 0) {
            return PAGE_HEADER_SIZE; // 从页面头部之后开始
        }
        return freeSpaceOffset;
    }

    /**
     * 设置页面中空闲空间的起始位置
     *
     * @param pageData 页面数据
     * @param offset 空闲空间起始位置
     */
    private static void setFreeSpaceOffset(byte[] pageData, int offset) {
        ByteBuffer.wrap(pageData).putInt(FREE_SPACE_OFFSET, offset);
    }

    /**
     * 获取指定索引位置的记录
     *
     * @param pageData 页面数据
     * @param index 记录索引
     * @return 记录对象，如果不存在则返回null
     */
    private static Record getRecordAt(byte[] pageData, int index) {
        try {
            // 从目录中获取记录偏移量
            int directoryEntryOffset = DIRECTORY_OFFSET + (index * 4);
            int recordOffset = ByteBuffer.wrap(pageData).getInt(directoryEntryOffset);

            // 读取记录长度
            int recordLength = ByteBuffer.wrap(pageData).getInt(recordOffset);

            // 读取记录数据
            byte[] recordData = new byte[recordLength];
            System.arraycopy(pageData, recordOffset + 4, recordData, 0, recordLength);

            // 反序列化记录
            return deserializeRecord(recordData);
        } catch (Exception e) {
            logger.error("Error reading record at index {} from page data", index, e);
            return null;
        }
    }

    /**
     * 序列化记录为字节数组
     *
     * @param record 记录对象
     * @return 序列化后的字节数组
     */
    private static byte[] serializeRecord(Record record) {
        // 简单实现：将记录转换为字符串再转为字节数组
        // 实际实现中可以使用更高效的序列化方式
        return record.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从字节数组反序列化记录
     *
     * @param data 字节数组
     * @return 记录对象
     */
    private static Record deserializeRecord(byte[] data) {
        // 简单实现：从字符串反序列化
        // 实际实现中需要根据序列化方式做相应的反序列化
        String recordString = new String(data, StandardCharsets.UTF_8);
        // 注意：这是一个简化的实现，实际项目中需要更复杂的反序列化逻辑
        return null;
    }

    /**
     * 初始化新页面的头部信息
     *
     * @param page 页面对象
     */
    public static void initializePageHeader(Page page) {
        byte[] pageData = page.getData();

        // 初始化记录数量为0
        setRecordCount(pageData, 0);

        // 初始化空闲空间位置
        setFreeSpaceOffset(pageData, PAGE_HEADER_SIZE);

        // 标记页面为脏页
        page.setDirty(true);

        logger.debug("Page {} header initialized", page.getPageId());
    }
}
