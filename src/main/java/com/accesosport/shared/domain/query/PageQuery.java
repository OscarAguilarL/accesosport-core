package com.accesosport.shared.domain.query;

public record PageQuery(int page, int size) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public PageQuery {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > MAX_SIZE)
            throw new IllegalArgumentException("size must be between 1 and " + MAX_SIZE);
    }

    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size);
    }

    public static PageQuery defaults() {
        return new PageQuery(0, DEFAULT_SIZE);
    }
}
