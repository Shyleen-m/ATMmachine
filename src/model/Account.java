package model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// OOP - Encapsulation: Account stores all customer-related data and controls access via getters/setters
// OOP - Single Responsibility: Manages only account data and transaction history
public class Account {
    private String owner;                // Encapsulated account owner name
    private String pin;                  // Encapsulated PIN for authentication
    private double balance;              // Encapsulated account balance
    private List<String> transactions;   // Encapsulated transaction history

    // Constructor: Initializes account data and empty transaction list
    public Account(String owner, String pin, double balance) {
        this.owner = owner;
        this.pin = pin;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    // -------------------- GETTERS / SETTERS --------------------
    // Encapsulation: Direct access to private fields is not allowed
    public String getOwner() { return owner; }
    public String getPin() { return pin; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public List<String> getTransactions() { return transactions; }

    // -------------------- TRANSACTION LOGGING --------------------
    // Encapsulation & SRP: Account keeps track of its own transaction history
    public void addTransaction(String type, double amount) {
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Log format: timestamp | transaction type | amount | current balance
        String entry = time + " | " + type + ": €" + String.format("%.2f", amount)
                + " | Balance: €" + String.format("%.2f", this.balance);
        transactions.add(entry);
    }

    // -------------------- SERIALIZATION --------------------
    // Converts account with transactions into JSON string (Encapsulation + Abstraction)
    // Could be extended/replaced with proper JSON library (Open/Closed Principle)
    public String toJsonWithTransactions() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"owner\":\"").append(owner).append("\", ");
        sb.append("\"pin\":\"").append(pin).append("\", ");
        sb.append("\"balance\":").append(String.format("%.2f", balance)).append(", ");
        sb.append("\"transactions\":[");
        for (int i = 0; i < transactions.size(); i++) {
            sb.append("\"").append(transactions.get(i)).append("\"");
            if (i < transactions.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }
}

