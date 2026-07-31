package com.expensetracker.dto;

public class CategoryTotalResponse {

    private String category;
    private double total;

    public CategoryTotalResponse() {
    }

    public CategoryTotalResponse(String category, double total) {
        this.category = category;
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
