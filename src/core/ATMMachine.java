package core;

import interfaces.*;          // Using interfaces supports abstraction (OOP) and Dependency Inversion (SOLID)
import model.Account;         // Account model represents a real-world entity (OOP: Encapsulation)
import services.*;            // Service layer separates responsibilities (SOLID: Single Responsibility)
import java.util.List;
import java.util.Optional;

public class ATMMachine implements ICustomerActions, ITechActions {
    // OOP: This class implements multiple interfaces → Polymorphism
    // SOLID (Interface Segregation): Separate interfaces for customer vs technician actions

    private List<Account> accounts;     // Composition: ATM "has" accounts
    private double internalCash = 5000.0; // Encapsulation: ATM manages its own internal state
    private PrinterService printer;     // Composition: ATM depends on PrinterService
    private PersistenceService persistence = new PersistenceService();
    // SOLID (Single Responsibility): Persistence logic delegated to a separate service class

    private boolean isSessionActive = false; // Tracks session state (Encapsulation)
    private final String firmwareVersion = "1.1.1"; // Constant value (Encapsulation + immutability)

    public ATMMachine() {
        // Constructor initializes ATM state and restores persisted data
        // SOLID (Single Responsibility): Loading state is delegated to PersistenceService

        this.accounts = persistence.loadAccounts(); // Abstraction: ATM doesn't know how loading happens

        // Load consumable resources from storage
        int savedPaper = persistence.loadPaperLevel();
        int savedInk = persistence.loadInkLevel();

        // Default fallback values if persistence returns invalid data
        if (savedPaper <= 0) savedPaper = 3;
        if (savedInk <= 0) savedInk = 3;

        this.printer = new PrinterService(savedPaper, savedInk);
        // Composition: ATM is composed of a PrinterService object
    }

    // -------------------- CUSTOMER --------------------

    public Account authenticateUser(String name, String pin) {
        // Abstraction: Method hides authentication logic from caller
        // Encapsulation: Direct account list access stays inside ATM

        if (isOutOfService()) { // Reuse of internal logic (modularity)
            System.out.println("[!] ATM out of service. Please try later.");
            return null;
        }

        // Input validation (Defensive programming)
        if (pin == null || !pin.matches("\\d{4}")) {
            System.out.println("[!] PIN must be exactly 4 digits.");
            return null;
        }

        // Search for existing account (Encapsulation of data access)
        for (Account a : accounts) {
            if (a.getOwner().equalsIgnoreCase(name)) {
                if (a.getPin().equals(pin)) {
                    isSessionActive = true; // State management
                    System.out.println("Welcome back, " + name + "!");
                    System.out.println("Current balance: €" + String.format("%.2f", a.getBalance()));
                    return a; // Polymorphism via Account object abstraction
                } else {
                    System.out.println("[!] Incorrect PIN.");
                    return null;
                }
            }
        }

        // Auto-register new user (Open/Closed Principle could be improved here by separating registration logic)
        Account newUser = new Account(name, pin, 0.0);
        accounts.add(newUser);
        persistence.saveState(accounts, internalCash, printer.getPaperLevel(), printer.getInkLevel());
        System.out.println("[+] Registered new account: " + name);
        isSessionActive = true;
        return newUser;
    }

    public boolean isOutOfService() {
        // Abstraction: Hides serviceability logic
        // Business rule: ATM can't operate without printer resources
        return printer.getPaperLevel() <= 0 || printer.getInkLevel() <= 0;
    }

    public boolean checkPaperInkWarning(java.util.Scanner sc) {
        // User interaction separated from business logic (SRP partially respected)
        if (printer.getPaperLevel() <= 3 || printer.getInkLevel() <= 3) {
            System.out.println("[!] Low paper/ink. Continue transaction? (y/n)");
            String resp = sc.nextLine();
            return resp.equalsIgnoreCase("y");
        }
        return true;
    }

    public double checkBalance(String name) {
        // Functional style with Optional improves null safety
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();

        return acc.map(Account::getBalance).orElse(0.0);
        // Encapsulation: Only balance is exposed, not the whole object
    }

    public void deposit(String name, int amount) {
        // Business logic isolated inside ATM (Encapsulation)
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();

        if (acc.isPresent()) {
            Account a = acc.get();
            a.setBalance(a.getBalance() + amount); // Encapsulation of Account state
            internalCash += amount; // ATM internal state updated
            persistence.saveState(accounts, internalCash, printer.getPaperLevel(), printer.getInkLevel());
            System.out.println("Successfully deposited €" + amount);
        }
    }

    public boolean withdraw(String name, int amount) {
        // Encapsulated withdrawal logic
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();

        if (acc.isPresent()) {
            Account a = acc.get();
            // Business rule enforcement
            if (a.getBalance() >= amount && internalCash >= amount) {
                a.setBalance(a.getBalance() - amount);
                internalCash -= amount;
                persistence.saveState(accounts, internalCash, printer.getPaperLevel(), printer.getInkLevel());
                printReceipt(); // Reuse of behavior (modularity)
                return true;
            } else {
                System.out.println("[!] Insufficient funds or ATM cash.");
            }
        }
        return false;
    }

    public void printReceipt() {
        // Delegation: PrinterService handles resource tracking (SRP)
        if (!printer.hasPaper()) {
            System.out.println("[!] Printer empty. Cannot print receipt.");
            return;
        }
        if (!printer.hasInk()) {
            System.out.println("[!] Printer out of ink. Cannot print receipt.");
            return;
        }

        printer.usePaper(); // Encapsulation: printer manages its own state
        printer.useInk();
        System.out.println("[*] Receipt printed.");

        // Warning system for low resources
        if (printer.getPaperLevel() <= 3) System.out.println("[!] Warning: Low paper.");
        if (printer.getInkLevel() <= 3) System.out.println("[!] Warning: Low ink.");

        persistence.saveState(accounts, internalCash, printer.getPaperLevel(), printer.getInkLevel());
    }

    public void logout() {
        isSessionActive = false; // Session state management (Encapsulation)
    }

    // -------------------- TECHNICIAN --------------------

    public boolean authenticateTech(String id, String pass) {
        // Simple hardcoded authentication (could be improved using Open/Closed Principle)
        return id.equals("TECH1") && pass.equals("123");
    }

    public void viewATMStatus() {
        // Separation of concerns: Status reporting is different from transaction logic
        System.out.println("\n--- ATM SYSTEM STATUS ---");
        System.out.println("System Status: ONLINE");
        System.out.println("Usage Status:  " + (isSessionActive ? "IN USE" : "IDLE"));
        System.out.println("Cash Level:    €" + internalCash);
        System.out.println("Paper Level:   " + printer.getPaperLevel() + " sheets");
        System.out.println("Ink Level:     " + printer.getInkLevel() + " units");
        System.out.println("Firmware:      " + firmwareVersion);
    }
}
