package com.doctusoft.ddd.storage;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import static java.util.Objects.*;

@Value
@Builder
public class BinaryContent implements FileContent {
    
    @NonNull String fileName;
    
    @NonNull byte[] content;
    
    public BinaryContent(String fileName, byte[] content) {
        this.fileName = requireNonNull(fileName, "fileName");
        this.content = requireNonNull(content, "content");
    }
    
}
