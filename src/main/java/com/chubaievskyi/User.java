package com.chubaievskyi;

import java.time.LocalDate;

@MyValidation
public class User {

    private String name;
    private String eddr;
    private int count;
    private LocalDate createdAt;

    public User() {
    }

    public User(String name, String eddr, int count, LocalDate createdAt) {
        this.name = name;
        this.eddr = eddr;
        this.count = count;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEddr() {
        return eddr;
    }

    public void setEddr(String eddr) {
        this.eddr = eddr;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}