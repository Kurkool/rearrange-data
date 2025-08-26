package org.example.model;

import java.util.List;

// --- Input Models ---
public class InputFile {
    private int cardTotal;
    private List<Card> cards;

    // Getters and Setters
    public int getCardTotal() { return cardTotal; }
    public void setCardTotal(int cardTotal) { this.cardTotal = cardTotal; }
    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }
}