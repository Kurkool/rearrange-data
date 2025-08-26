package org.example.model;

import java.util.List;

public class OutputFile {
    private int accountTotal;
    private List<Account> accounts;

    public OutputFile(int accountTotal, List<Account> accounts) {
        this.accountTotal = accountTotal;
        this.accounts = accounts;
    }

    // Getters and Setters
    public int getAccountTotal() { return accountTotal; }
    public void setAccountTotal(int accountTotal) { this.accountTotal = accountTotal; }
    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }
}
