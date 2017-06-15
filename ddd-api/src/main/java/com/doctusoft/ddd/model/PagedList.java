package com.doctusoft.ddd.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.requireNonNull;

@Data
@AllArgsConstructor
public final class PagedList<T> implements PagedResponse<T>, Serializable {
    
    public static <T> PagedList<T> empty() { return new PagedList<>(Collections.emptyList(), 0); }
    
    private List<T> pageRows;
    
    private int totalRowCount;
    
    public <U> PagedList<U> transform(Function<? super T, ? extends U> mapperFun) {
        requireNonNull(mapperFun, "mapperFun");
        return new PagedList<>(pageRows.stream().map(mapperFun).collect(Collectors.toList()), totalRowCount);
    }
    
}
