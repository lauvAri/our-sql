package storage.service;

public enum FileType {
    SCHEMA(0),
    DATA(1),
    INDEX(2);

    private int fileType;
    FileType(int _fileType) {
        fileType = _fileType;
    }
}
