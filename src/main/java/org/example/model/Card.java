package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    private String productName;
    private String cardNumber;
    private String accountNumber;
    private String balance;

    @JsonProperty("expire-date")
    private String expireDate;

    // Getters and Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getBalance() { return balance; }
    public void setBalance(String balance) { this.balance = balance; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
}
