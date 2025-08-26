package org.example.model;

import java.util.List;

public class Product {
    private String name;
    private String totalBalance;
    private List<CardDetail> details;

    public Product(String name, String totalBalance, List<CardDetail> details) {
        this.name = name;
        this.totalBalance = totalBalance;
        this.details = details;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTotalBalance() { return totalBalance; }
    public void setTotalBalance(String totalBalance) { this.totalBalance = totalBalance; }
    public List<CardDetail> getDetails() { return details; }
    public void setDetails(List<CardDetail> details) { this.details = details; }
}
