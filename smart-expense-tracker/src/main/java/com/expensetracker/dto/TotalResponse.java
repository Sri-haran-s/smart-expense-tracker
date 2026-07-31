package com.expensetracker.dto;

public class TotalResponse {

    private double total;

    public TotalResponse() {
    }

    public TotalResponse(double total) {
        this.total = total;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
