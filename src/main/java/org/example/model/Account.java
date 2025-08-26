package org.example.model;

import java.util.List;

public class Account {
    private String accountNumber;
    private List<Product> products;

    public Account(String accountNumber, List<Product> products) {
        this.accountNumber = accountNumber;
        this.products = products;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
