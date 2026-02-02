package core;

import interfaces.ICustomerActions;     // Interface Segregation: customer-specific behavior
import interfaces.ITechActions;         // Interface Segregation: technician-specific behavior
import interfaces.IATMStateService;     // Dependency Inversion: ATM depends on abstraction, not concrete persistence
import model.Account;                   // Domain model (OOP: Encapsulation)
import services.PrinterService;         // Service responsible only for printing resources (SRP)

import java.util.List;
import java.util.Optional;

public class ATMMachineV2 implements ICustomerActions, ITechActions {
    // Polymorphism: One class implementing multiple behavior contracts
    // Interface Segregation Principle (SOLID) in action

    private List<Account> accounts;     // Composition: ATM "has" many accounts
    private double internalCash;        // Encapsulated machine cash state
    private PrinterService printer;     // Composition: ATM "has a" printer
    private IATMStateService persistence; // Dependency Inversion: abstraction instead of concrete class
    private boolean isSessionActive;    // Session state tracking
    private String firmwareVersion;     // Firmware stored as part of ATM system state

    public ATMMachineV2(IATMStateService persistence, PrinterService printer) {
        // Constructor Injection → SOLID (Dependency Inversion Principle)
        // ATM does NOT create its dependencies — they are provided from outside

        this.persistence = persistence;
        this.printer = printer;

        // Abstraction: ATM does not know HOW data is stored, only that it can be loaded
        this.accounts = persistence.loadAccounts();
        this.internalCash = persistence.loadCashLevel();
        this.printer.setPaperLevel(persistence.loadPaperLevel());
        this.printer.setInkLevel(persistence.loadInkLevel());
        this.firmwareVersion = persistence.loadFirmwareVersion();

        this.isSessionActive = false; // Initial state
    }

    // ------------------- CUSTOMER ACTIONS -------------------

    public Account authenticateUser(String name, String pin) {
        // Abstraction: Hides authentication process
        // Encapsulation: Account data accessed only through ATM logic

        if (isOutOfService()) {
            System.out.println("[!] ATM out of service. Please try later.");
            return null;
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            System.out.println("[!] PIN must be 4 digits.");
            return null;
        }

        // Searching account list (Encapsulation of data)
        for (Account a : accounts) {
            if (a.getOwner().equalsIgnoreCase(name)) {
                if (a.getPin().equals(pin)) {
                    isSessionActive = true; // State change
                    System.out.println("Welcome, " + name + "!");
                    System.out.println("Current balance: €" + String.format("%.2f", a.getBalance()));
                    return a;
                } else {
                    System.out.println("[!] Incorrect PIN.");
                    return null;
                }
            }
        }

        // New user registration
        // Could be separated into a RegistrationService for stronger SRP
        Account newUser = new Account(name, pin, 0.0);
        accounts.add(newUser);
        saveState(); // Centralized persistence
        System.out.println("[+] Registered new account: " + name);
        isSessionActive = true;
        return newUser;
    }

    public boolean isOutOfService() {
        // Business rule abstraction
        // ATM cannot function without cash or printing capability
        return internalCash <= 0 || printer.getPaperLevel() <= 0 || printer.getInkLevel() <= 0;
    }

    public boolean checkPaperInkWarning(java.util.Scanner sc) {
        // UI interaction mixed with logic (minor SRP violation, but acceptable for console apps)
        if (printer.getPaperLevel() <= 3 || printer.getInkLevel() <= 3) {
            System.out.println("[!] Low paper/ink. Continue transaction? (y/n)");
            String resp = sc.nextLine();
            return resp.equalsIgnoreCase("y");
        }
        return true;
    }

    public double checkBalance(String name) {
        // Functional programming style improves readability & null safety
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();
        return acc.map(Account::getBalance).orElse(0.0);
    }

    public void deposit(String name, int amount) {
        // Encapsulation of transaction logic
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();

        acc.ifPresent(a -> {
            a.setBalance(a.getBalance() + amount);
            internalCash += amount; // ATM state updated
            saveState(); // Persistence abstraction
            System.out.println("Successfully deposited €" + amount);
        });
    }

    public boolean withdraw(String name, int amount) {
        // Business logic encapsulated within ATM
        Optional<Account> acc = accounts.stream()
                .filter(a -> a.getOwner().equalsIgnoreCase(name))
                .findFirst();

        if (acc.isPresent()) {
            Account a = acc.get();
            // Business rule validation
            if (a.getBalance() >= amount && internalCash >= amount) {
                a.setBalance(a.getBalance() - amount);
                internalCash -= amount;
                System.out.println("Desired amount reached. Please collect your cash: €" + amount);
                printReceipt(); // Behavior reuse (modularity)
                saveState();
                return true;
            } else {
                System.out.println("[!] Insufficient funds or ATM cash.");
            }
        }
        return false;
    }

    public void printReceipt() {
        // Check if printer can print at all
        if (!printer.hasPaper() || !printer.hasInk()) {
            System.out.println("[!] Printer depleted. ATM out of service. Logging out user...");
            logout();
            saveState();
            return;
        }

        // Use resources
        printer.usePaper();
        printer.useInk();

        // Print receipt
        System.out.println("[*] Receipt printed.");

        // Warnings if low but not depleted
        if (printer.hasPaper() && printer.getPaperLevel() <= 3) System.out.println("[!] Warning: Low paper.");
        if (printer.hasInk() && printer.getInkLevel() <= 3) System.out.println("[!] Warning: Low ink.");

        // If resources depleted after printing
        if (!printer.hasPaper() || !printer.hasInk()) {
            System.out.println("[!] Printer depleted after printing. ATM out of service. Logging out user...");
            logout();
        }

        // Save state
        saveState();
    }


    public void logout() {
        isSessionActive = false; // Encapsulated session state control
    }

    // ------------------- TECHNICIAN ACTIONS -------------------

    public boolean authenticateTech(String id, String pass) {
        // Hardcoded credentials (not scalable → violates Open/Closed if expanded)
        return id.equals("TECH1") && pass.equals("123");
    }

    public void viewATMStatus() {
        // Separation of concerns: Monitoring vs transaction handling
        System.out.println("\n--- ATM STATUS ---");
        System.out.println("System: ONLINE");
        System.out.println("Usage: " + (isSessionActive ? "IN USE" : "IDLE"));
        System.out.println("Cash: €" + internalCash);
        System.out.println("Paper: " + printer.getPaperLevel() + " sheets");
        System.out.println("Ink: " + printer.getInkLevel() + " units");
        System.out.println("Firmware: " + firmwareVersion);
    }

    // ------------------- TECHNICIAN METHODS -------------------

    public void refillCash(int amount) {
        internalCash += amount; // Encapsulated resource control
        saveState();
        System.out.println("[+] Cash refilled: €" + amount);
    }

    public void collectCash(int amount) {
        if (amount > internalCash) amount = (int) internalCash; // Business rule safeguard
        internalCash -= amount;
        saveState();
        System.out.println("[+] Cash collected: €" + amount);
    }

    public void refillPaper(int sheets) {
        printer.setPaperLevel(printer.getPaperLevel() + sheets); // Delegation to PrinterService
        saveState();
        System.out.println("[+] Paper refilled: " + sheets + " sheets");
    }

    public void refillInk(int units) {
        printer.setInkLevel(printer.getInkLevel() + units); // Delegation to PrinterService
        saveState();
        System.out.println("[+] Ink refilled: " + units + " units");
    }

    public void updateFirmware(String version) {
        // Validation logic (Encapsulation of system rules)
        if (version == null || !version.matches("\\d+\\.\\d+\\.\\d+")) {
            System.out.println("[!] Invalid firmware format. Use X.Y.Z (e.g., 1.1.1)");
            return;
        }
        this.firmwareVersion = version; // Encapsulation of firmware state
        saveState();
        System.out.println("[+] Firmware updated to " + version);
    }

    // ------------------- SAVE STATE -------------------

    private void saveState() {
        // Abstraction + Dependency Inversion
        // ATM does not know how or where data is stored
        persistence.saveState(accounts, internalCash, printer.getPaperLevel(), printer.getInkLevel(), firmwareVersion);
    }

    // ------------------- GETTERS -------------------
    // Controlled exposure of internal state (Encapsulation)

    public double getCashAvailable() { return internalCash; }
    public int getPaperAvailable() { return printer.getPaperLevel(); }
    public int getInkAvailable() { return printer.getInkLevel(); }
    public String getFirmwareVersion() { return firmwareVersion; }
}
