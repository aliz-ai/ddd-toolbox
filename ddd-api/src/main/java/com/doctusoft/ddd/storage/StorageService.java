package com.doctusoft.ddd.storage;

import com.doctusoft.ddd.model.EntityKey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.doctusoft.java.Failsafe.checkArgument;

public interface StorageService {
    
    <T extends FileContent> EntityKey<StorageObject> createArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String archiveBaseName);
    
    EntityKey<StorageObject> createFile(EntityKey<?> referencingEntity, String category, FileContent fileContent, String mimeType);
    
    <T extends FileContent> EntityKey<StorageObject> createFileOrArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String archiveBaseName, String mimeType);
    
    FileDownloadResponse download(EntityKey<StorageObject> key);
    
    int COMPRESSION_THRESHOLD = 1 << 11;
    
    static byte[] compress(byte[] uncompressed) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(getBufferSize(uncompressed.length));
            GZIPOutputStream zipStream = new GZIPOutputStream(buffer)) {
            zipStream.write(uncompressed);
            zipStream.finish();
            zipStream.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    static byte[] decompress(byte[] compressed, long uncompressedSize) {
        int bufferSize = getBufferSize(uncompressedSize);
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
            GZIPInputStream zipStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buf = new byte[bufferSize];
            while (true) {
                int r = zipStream.read(buf);
                if (r == -1) {
                    break;
                }
                buffer.write(buf, 0, r);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    static int getBufferSize(long exactSize) {
        checkArgument(exactSize >= 0, () -> "Negative size: " + exactSize);
        int bufferSize = DEFAULT_BUFFER_SIZE;
        if (exactSize > (long) bufferSize) {
            return bufferSize;
        }
        return (int) exactSize;
    }
    
    static byte[] getDownloadContent(StorageObject storageObject) {
        switch (storageObject.getStorageMode()) {
        case COMPRESSED:
            return decompress(storageObject.getStoredContent(), storageObject.getUncompressedSize());
        default:
            return storageObject.getStoredContent();
        }
    }
    
    int DEFAULT_BUFFER_SIZE = 1 << 16; // 64kB
    
}
