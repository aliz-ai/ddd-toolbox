package com.doctusoft.ddd.hibernate.storage;

import com.doctusoft.ddd.storage.StorageObject;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Proxy(proxyClass = StorageObject.class)
@Immutable
public class StorageObjectImpl implements StorageObject {
    
    @Id
    @Column(nullable = false, length = 36)
    private String id;
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false, length = 255)
    private String category;
    
    @Column(nullable = false, length = 255)
    private String mimeType;
    
    @Column(nullable = false, length = 255)
    private String fileName;
    
    @Column(nullable = true, length = 32)
    private String encoding;
    
    @Column(nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private StorageMode storageMode;
    
    @Column(nullable = true)
    private Integer archiveEntriesCount;

    @Column(nullable = false)
    private Long uncompressedSize;
    
    @Column(nullable = false)
    private Long storedContentSize;
    
    @Column(nullable = false, length = 255)
    private String entityReference;
    
    @Column(nullable = false)
    private byte[] storedContent;
    
}
