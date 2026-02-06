package com.aditya.simple_web_app.web_app.dto;

import java.util.List;

public class PaginatedResponse<T>{

    private List<T> data;
    PaginationMeta paginationMeta;

    public PaginatedResponse(List<T> data, PaginationMeta paginationMeta) {
        this.data = data;
        this.paginationMeta = paginationMeta;
    }

    public List<T> getData() {
        return data;
    }

    public PaginationMeta getPaginationMeta() {
        return paginationMeta;
    }
}
