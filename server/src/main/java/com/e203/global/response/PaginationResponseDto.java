package com.e203.global.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PaginationResponseDto<T> {

    private int currentPage;

    private int pageSize;

    private long totalCount;

    private int totalPages;

    private List<T> data;

    @Builder
    private PaginationResponseDto(int currentPage, int pageSize, long totalCount, int totalPages, List<T> data) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.data = data;
    }

}
