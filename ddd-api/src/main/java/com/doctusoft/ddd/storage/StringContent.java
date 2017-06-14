package com.doctusoft.ddd.storage;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.nio.charset.Charset;

import static java.util.Objects.*;

@Value
@Builder
public class StringContent implements TextContent {
    
    public static StringContent parse(String fileName, Charset encoding, byte[] binaryContent) {
        String textContent = new String(binaryContent, encoding);
        return new StringContent(fileName, encoding, textContent);
    }
    
    @NonNull String fileName;
    
    @NonNull Charset encoding;
    
    @NonNull String textContent;
    
    public StringContent(String fileName, Charset encoding, String textContent) {
        this.fileName = requireNonNull(fileName, "fileName");
        this.encoding = requireNonNull(encoding, "encoding");
        this.textContent = requireNonNull(textContent, "textContent");
    }
    
}
