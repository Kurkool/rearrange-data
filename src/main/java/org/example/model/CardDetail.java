package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CardDetail {
    private String cardNumber;
    private String balance;

    @JsonProperty("expire-date")
    private String expireDate;

    public CardDetail(String cardNumber, String balance, String expireDate) {
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.expireDate = expireDate;
    }

    // Getters and Setters
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getBalance() { return balance; }
    public void setBalance(String balance) { this.balance = balance; }
    public String getExpireDate() { return expireDate; }
    public void setExpireDate(String expireDate) { this.expireDate = expireDate; }
}
