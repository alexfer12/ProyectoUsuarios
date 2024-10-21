package com.example.mediumRoles.exceptions;


import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

public class CustomPagedResponse<T> extends RepresentationModel<CustomPagedResponse<T>> {
    private List<T> content;
    private int size;
    private long totalElements;
    private int totalPages;
    private int number;

    public CustomPagedResponse(List<T> content, int size, long totalElements, int totalPages, int number) {
        this.content = content;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
    }

    // Getters y Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}