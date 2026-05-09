package org.aistemsplitter;

public final class Credits {
    private final int balance;
    private final String unit;

    public Credits(int balance, String unit) {
        this.balance = balance;
        this.unit = unit;
    }

    public int getBalance() {
        return balance;
    }

    public String getUnit() {
        return unit;
    }
}
