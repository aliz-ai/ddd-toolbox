package com.doctusoft.ddd.storage;

import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.model.Instantiator;
import com.doctusoft.ddd.persistence.GenericPersistence;
import com.doctusoft.java.RandomId;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.doctusoft.java.Failsafe.checkArgument;
import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SimpleStorageServiceImpl implements StorageService {
    
    private final GenericPersistence persistence;
    
    private final Instantiator instantiator;
    
    private StorageObject initObject(EntityKey<?> referencingEntity, String category, String mimeType) {
        requireNonNull(referencingEntity, "entityReference");
        requireNonNull(category, "category");
        requireNonNull(mimeType, "mimeType");
        StorageObject instance = instantiator.instantiate(StorageObject.class);
        instance.setId(RandomId.sequential());
        instance.setCreatedAt(Instant.now());
        instance.setEntityReference(referencingEntity.toString());
        instance.setCategory(category);
        instance.setMimeType(mimeType);
        return instance;
    }
    
    public <T extends FileContent> StorageObject createArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String archiveBaseName) {
        StorageObject storageObject = initObject(referencingEntity, category, "application/zip");
        requireNonNull(archiveEntries, "archiveEntries");
        int archiveEntriesCount = archiveEntries.size();
        checkArgument(archiveEntriesCount > 0, "No entries to archive");
        requireNonNull(archiveBaseName, "archiveBaseName");
        storageObject.setFileName(archiveBaseName + ".zip");
        storageObject.setStorageMode(StorageObject.StorageMode.ARCHIVE);
        storageObject.setArchiveEntriesCount(archiveEntriesCount);
        
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(getBufferSize());
            ZipOutputStream zipArchive = new ZipOutputStream(buffer)) {
            
            long uncompressedSize = 0;
            Charset charset = null;
            boolean uniformEncoding = true;
            
            for (FileContent entry : archiveEntries) {
                String fileName = requireNonNull(entry.getFileName(), "fileName");
                byte[] content = entry.getContent();
                uncompressedSize += (long) content.length;
                
                zipArchive.putNextEntry(new ZipEntry(fileName));
                zipArchive.write(content);
                
                if (entry instanceof TextContent) {
                    if (uniformEncoding) {
                        TextContent textFile = (TextContent) entry;
                        Charset fileEncoding = requireNonNull(textFile.getEncoding(), "textFile.encoding");
                        if (charset == null) {
                            charset = fileEncoding;
                        } else {
                            uniformEncoding = charset.equals(fileEncoding);
                        }
                    }
                } else {
                    uniformEncoding = false;
                }
            }
            zipArchive.finish();
            
            storageObject.setUncompressedSize(uncompressedSize);
            storageObject.setStoredContent(buffer.toByteArray());
            storageObject.setStoredContentSize((long) storageObject.getStoredContent().length);
            if (uniformEncoding) {
                storageObject.setEncoding(charset.name());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        persistence.insert(storageObject);
        return storageObject;
    }
    
    public StorageObject createFile(EntityKey<?> referencingEntity, String category, FileContent fileContent, String mimeType) {
        requireNonNull(fileContent, "fileContent");
        StorageObject storageObject = initObject(referencingEntity, category, mimeType);
        storageObject.setFileName(requireNonNull(fileContent.getFileName(), "fileName"));
        byte[] uncompressedContent = fileContent.getContent();
        storageObject.setUncompressedSize((long) uncompressedContent.length);
        if (uncompressedContent.length < COMPRESSION_THRESHOLD) {
            storageObject.setStorageMode(StorageObject.StorageMode.UNCOMPRESSED);
            storageObject.setStoredContent(uncompressedContent);
        } else {
            storageObject.setStorageMode(StorageObject.StorageMode.COMPRESSED);
            storageObject.setStoredContent(compress(uncompressedContent));
        }
        int storedConentSize = storageObject.getStoredContent().length;
        storageObject.setStoredContentSize((long) storedConentSize);
        if (fileContent instanceof TextContent) {
            TextContent textContent = (TextContent) fileContent;
            storageObject.setEncoding(textContent.getEncoding().name());
        }
        persistence.insert(storageObject);
        return storageObject;
    }
    
    public <T extends FileContent> StorageObject createFileOrArchive(EntityKey<?> referencingEntity, String category, Collection<T> archiveEntries, String archiveBaseName, String mimeType) {
        if (archiveEntries.size() == 1) {
            T singleEntry = archiveEntries.iterator().next();
            return createFile(referencingEntity, category, singleEntry, mimeType);
        } else {
            return createArchive(referencingEntity, category, archiveEntries, archiveBaseName);
        }
    }
    
    public FileDownloadResponse download(EntityKey<StorageObject> key) {
        return StorageService.getDownloadRespone(persistence.require(key));
    }
    
    protected byte[] compress(byte[] uncompressed) {
        return StorageService.compress(uncompressed);
    }
    
    protected byte[] decompress(byte[] compressed, long uncompressedSize) {
        return StorageService.decompress(compressed, uncompressedSize);
    }
    
    protected int getBufferSize() { return DEFAULT_BUFFER_SIZE; }
    
}
