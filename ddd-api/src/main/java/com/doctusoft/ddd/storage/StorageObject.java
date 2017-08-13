package com.doctusoft.ddd.storage;

import com.doctusoft.ddd.model.CreatedAt;
import com.doctusoft.ddd.model.EntityKey;
import com.doctusoft.ddd.model.EntityWithStringId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StorageObject extends EntityWithStringId, CreatedAt {
    
    @NotNull static EntityKey<StorageObject> createKey(String id) { return EntityKey.create(StorageObject.class, id); }
    
    @NotNull default EntityKey<StorageObject> getKey() { return createKey(getId()); }
    
    @NotNull default Class<StorageObject> getKind() { return StorageObject.class; }
    
    /**
     * The {@link EntityKey#toString() EntityKey} of the entity referencing the storage object.
     */
    String getEntityReference();
    
    void setEntityReference(String entityReference);
    
    /**
     * Specifier for the referenced entity to identify the type/purpose of this object (e.G. if one entity references more objects).
     */
    String getCategory();
    
    void setCategory(String category);
    
    /**
     * The fileName (with extension) that must be used when downloading this file.
     */
    String getFileName();
    
    void setFileName(String fileName);
    
    /**
     * The valid mimeType set when downloading this file.
     */
    String getMimeType();
    
    void setMimeType(String mimeType);
    
    /**
     * <code>null</code> for binary content, e.G. pdf files, but may be defined for archive files if we know the
     * encoding of the files inside the archive. Just a hint on which encoding to use for reading the content.
     */
    @Nullable String getEncoding();
    
    void setEncoding(String encoding);
    
    enum StorageMode {
        
        /**
         * Several files are stored in a zip archive. The downloaded content is the zipfile.
         */
        ARCHIVE,
        
        /**
         * The content of a single file is stored, compressed automatically. The downloaded content is the uncompressed
         * original content.
         */
        COMPRESSED,
        
        /**
         * The content of the file is stored as is (uncompressed).
         */
        UNCOMPRESSED;
    }
    
    StorageMode getStorageMode();
    
    void setStorageMode(StorageMode storageMode);
    
    /**
     * If the object is an archive (e.G. zip file) and contains several files, we keep track of the number of the files
     * inside the archive.
     */
    @Nullable Integer getArchiveEntriesCount();
    
    void setArchiveEntriesCount(Integer archiveEntriesCount);
    
    /**
     * The size in bytes of the {@link #getStoredContent()} attribute of this object.
     */
    Long getStoredContentSize();
    
    void setStoredContentSize(Long storedContentSize);
    
    /**
     * The total size in bytes of the uncompressed content of the stored object.
     */
    Long getUncompressedSize();
    
    void setUncompressedSize(Long uncompressedSize);
    
    /**
     * The compressed content of the stored object. If {@link #getArchiveEntriesCount()} is defined this represents the sum
     * of sizes of all extracted entries in bytes.
     */
    byte[] getStoredContent();
    
    void setStoredContent(byte[] storedContent);
    
}
