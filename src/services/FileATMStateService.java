package services;

import interfaces.IATMStateService;
import model.Account;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// Concrete implementation of IATMStateService using a JSON file
// OOP: Encapsulation of all file-based persistence logic
// SOLID - Single Responsibility Principle (SRP): This class handles only persistence
// SOLID - Dependency Inversion Principle (DIP): ATMMachineV2 depends on the IATMStateService abstraction, not this concrete class
// Liskov Substitution Principle (LSP): Any other implementation of IATMStateService can be substituted without breaking ATM behavior
public class FileATMStateService implements IATMStateService {

    private final String PATH = "data/atm_state.json"; // File path encapsulated

    // ---------------------- SAVE STATE ----------------------
    @Override
    public void saveState(List<Account> accounts, double cash, int paper, int ink, String firmware) {
        // Encapsulation: Builds JSON string from account data, ATM cash, printer levels, firmware
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"cash\": ").append(cash).append(",\n");
        sb.append("  \"paper\": ").append(paper).append(",\n");
        sb.append("  \"ink\": ").append(ink).append(",\n");
        sb.append("  \"firmware\":\"").append(firmware).append("\",\n");
        sb.append("  \"accounts\": [\n");

        for (int i = 0; i < accounts.size(); i++) {
            sb.append("    ").append(accounts.get(i).toJsonWithTransactions());
            if (i < accounts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");

        try {
            Files.createDirectories(Paths.get("data")); // Ensure folder exists
            Files.write(Paths.get(PATH), sb.toString().getBytes()); // Save JSON to file
        } catch (IOException e) {
            System.out.println("[!] Save Error: " + e.getMessage());
        }
    }

    // ---------------------- LOAD ACCOUNTS ----------------------
    @Override
    public List<Account> loadAccounts() {
        List<Account> list = new ArrayList<>();
        try {
            String data = Files.readString(Paths.get(PATH));

            int start = data.indexOf("[");
            int end = data.lastIndexOf("]");
            if (start >= 0 && end >= 0) {
                String accountsData = data.substring(start + 1, end).trim();
                if (!accountsData.isEmpty()) {
                    // Split JSON objects safely
                    String[] accs = accountsData.split("\\},\\{");
                    for (String accStr : accs) {
                        String a = accStr;
                        if (!a.startsWith("{")) a = "{" + a;
                        if (!a.endsWith("}")) a = a + "}";

                        // Extract basic fields using helper methods
                        String owner = extractStringField(a, "owner");
                        String pin = extractStringField(a, "pin");
                        double balance = extractDoubleField(a, "balance");

                        Account account = new Account(owner, pin, balance);

                        // Extract transactions if present
                        int tStart = a.indexOf("\"transactions\"");
                        if (tStart >= 0) {
                            int arrStart = a.indexOf("[", tStart);
                            int arrEnd = a.indexOf("]", arrStart);
                            if (arrStart >= 0 && arrEnd >= 0) {
                                String txBlock = a.substring(arrStart + 1, arrEnd).trim();
                                if (!txBlock.isEmpty()) {
                                    String[] txs = txBlock.split("\",\"");
                                    for (String tx : txs) {
                                        tx = tx.trim();
                                        tx = tx.replaceFirst("^\"", "").replaceFirst("\"$", "");
                                        if (!tx.isEmpty()) account.getTransactions().add(tx);
                                    }
                                }
                            }
                        }

                        list.add(account);
                    }
                }
            }
        } catch (Exception e) {
            // No state file or parse problem: create default preset account
            list.add(new Account("ngaa", "2006", 100.0));
            saveState(list, loadCashLevel(), 4, 4, loadFirmwareVersion());
            return list;
        }

        // Ensure preset account exists with correct minimum balance
        boolean hasPreset = list.stream().anyMatch(a -> a.getOwner().equalsIgnoreCase("ngaa") && a.getPin().equals("2006"));
        if (!hasPreset) {
            list.add(new Account("ngaa", "2006", 100.0));
            saveState(list, loadCashLevel(), loadPaperLevel(), loadInkLevel(), loadFirmwareVersion());
        } else {
            for (Account a : list) {
                if (a.getOwner().equalsIgnoreCase("ngaa") && a.getPin().equals("2006")) {
                    if (a.getBalance() < 100.0) {
                        a.setBalance(100.0);
                        saveState(list, loadCashLevel(), loadPaperLevel(), loadInkLevel(), loadFirmwareVersion());
                    }
                }
            }
        }

        return list;
    }

    // ---------------------- LOAD PAPER ----------------------
    @Override
    public int loadPaperLevel() {
        try {
            String data = Files.readString(Paths.get(PATH));
            for (String line : data.split("\n")) {
                if (line.contains("\"paper\"")) return Integer.parseInt(line.split(":")[1].replace(",", "").trim());
            }
        } catch (Exception e) {}
        return 4; // Default
    }

    // ---------------------- LOAD INK ----------------------
    @Override
    public int loadInkLevel() {
        try {
            String data = Files.readString(Paths.get(PATH));
            for (String line : data.split("\n")) {
                if (line.contains("\"ink\"")) return Integer.parseInt(line.split(":")[1].replace(",", "").trim());
            }
        } catch (Exception e) {}
        return 4; // Default
    }

    // ---------------------- LOAD CASH ----------------------
    @Override
    public double loadCashLevel() {
        try {
            String data = Files.readString(Paths.get(PATH));
            for (String line : data.split("\n")) {
                if (line.contains("\"cash\"")) return Double.parseDouble(line.split(":")[1].replace(",", "").trim());
            }
        } catch (Exception e) {}
        return 5000.0; // Default
    }

    // ---------------------- LOAD FIRMWARE ----------------------
    @Override
    public String loadFirmwareVersion() {
        try {
            String data = Files.readString(Paths.get(PATH));
            for (String line : data.split("\n")) {
                if (line.contains("\"firmware\""))
                    return line.split(":")[1].replace("\"", "").replace(",", "").trim();
            }
        } catch (Exception e) {}
        return "1.0.0"; // Default
    }

    // ---------------------- HELPER METHODS ----------------------
    // Parse string fields from JSON-like string
    private String extractStringField(String src, String field) {
        try {
            int idx = src.indexOf("\"" + field + "\"");
            if (idx < 0) return "";
            int colon = src.indexOf(":", idx);
            int firstQuote = src.indexOf('"', colon);
            int secondQuote = src.indexOf('"', firstQuote + 1);
            return src.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) { return ""; }
    }

    // Parse double fields from JSON-like string
    private double extractDoubleField(String src, String field) {
        try {
            int idx = src.indexOf("\"" + field + "\"");
            int colon = src.indexOf(":", idx);
            int end = src.indexOf(",", colon);
            if (end < 0) end = src.indexOf("}", colon);
            String num = src.substring(colon + 1, end).trim();
            return Double.parseDouble(num);
        } catch (Exception e) { return 0.0; }
    }
}
