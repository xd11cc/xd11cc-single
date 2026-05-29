package com.xd11cc.single.entity.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> rows;

    private long total;

    public PageResult(List<T> rows, long total) {
        this.rows = rows;
        this.total = total;
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0);
    }
}
