package services;

import model.Account;
import java.io.*;
import java.nio.file.*;
import java.util.*;

// PersistenceService handles saving/loading ATM data to a local JSON file
// OOP: Encapsulation of persistence logic
// SOLID - Single Responsibility Principle (SRP):
// This class is responsible only for reading/writing ATM state (accounts, cash, paper, ink)
// Note: Unlike FileATMStateService, this is a concrete service directly used by ATMMachine (less flexible)
public class PersistenceService {

    private final String PATH = "data/atm_state.json"; // Encapsulated file path

    // ---------------- SAVE STATE ----------------
    public void saveState(List<Account> accounts, double cash, int paper, int ink) {
        // Encapsulation: Builds JSON-like string representing ATM state
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"cash\": ").append(cash).append(",\n");
        sb.append("  \"paper\": ").append(paper).append(",\n");
        sb.append("  \"ink\": ").append(ink).append(",\n");
        sb.append("  \"accounts\": [\n");

        for (int i = 0; i < accounts.size(); i++) {
            sb.append("    ").append(accounts.get(i).toJsonWithTransactions()); // Encapsulation: Account handles its own JSON
            if (i < accounts.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");

        try {
            Files.createDirectories(Paths.get("data")); // Ensure directory exists
            Files.write(Paths.get(PATH), sb.toString().getBytes()); // Write JSON to file
        } catch (IOException e) {
            System.out.println("[!] Save Error: " + e.getMessage());
        }
    }

    // ---------------- LOAD ACCOUNTS ----------------
    public List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            StringBuilder content = new StringBuilder();
            for (String line : lines) content.append(line);
            String data = content.toString();

            // Simple parsing: extract "accounts":[...] block
            int start = data.indexOf("[");
            int end = data.lastIndexOf("]");
            if (start < 0 || end < 0) throw new Exception("Invalid file");

            String accountsData = data.substring(start + 1, end);
            String[] accountsSplit = accountsData.split("\\},\\{");

            for (String accStr : accountsSplit) {
                accStr = accStr.replace("{", "").replace("}", "").trim();
                String[] parts = accStr.split(",");
                String owner = "", pin = "";
                double balance = 0;

                for (String p : parts) {
                    p = p.trim();
                    if (p.startsWith("\"owner\"")) owner = p.split(":")[1].replace("\"", "").trim();
                    if (p.startsWith("\"pin\"")) pin = p.split(":")[1].replace("\"", "").trim();
                    if (p.startsWith("\"balance\"")) balance = Double.parseDouble(p.split(":")[1].trim());
                    // Transactions parsing skipped here (could be implemented later)
                }

                list.add(new Account(owner, pin, balance)); // Encapsulation: Account object stores its own state
            }
        } catch (Exception e) {
            // Fallback: default accounts if file is missing or invalid
            list.add(new Account("Alice", "1234", 1000.0));
            list.add(new Account("Bob", "5555", 500.0));
        }

        return list;
    }

    // ---------------- LOAD PAPER ----------------
    public int loadPaperLevel() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            for (String line : lines) {
                if (line.contains("\"paper\"")) {
                    return Integer.parseInt(line.split(":")[1].replace(",", "").trim());
                }
            }
        } catch (Exception e) { }
        return 10; // Default value if file missing or error
    }

    // ---------------- LOAD INK ----------------
    public int loadInkLevel() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PATH));
            for (String line : lines) {
                if (line.contains("\"ink\"")) {
                    return Integer.parseInt(line.split(":")[1].replace(",", "").trim());
                }
            }
        } catch (Exception e) { }
        return 10; // Default
    }
}
