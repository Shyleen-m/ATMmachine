package services;

// PrinterService models the ATM receipt printer
// OOP: Encapsulation of printer state and behavior
// SOLID - Single Responsibility Principle (SRP):
// Class handles only printer paper/ink levels and usage
// SOLID - Potential for Liskov Substitution Principle (LSP):
// Could be replaced with another implementation (e.g., ThermalPrinter, LaserPrinter) if it implements the same interface
public class PrinterService {

    private int paperLevel;  // Encapsulated: current number of paper sheets
    private int inkLevel;    // Encapsulated: current ink units

    // Constructor initializes printer with paper and ink
    public PrinterService(int paper, int ink) {
        this.paperLevel = paper;
        this.inkLevel = ink;
    }

    // ---------------- GETTERS ----------------
    // Encapsulation: external classes can check printer state without modifying it directly
    public int getPaperLevel() { return paperLevel; }
    public int getInkLevel() { return inkLevel; }
    public boolean hasPaper() { return paperLevel > 0; }
    public boolean hasInk() { return inkLevel > 0; }

    // ---------------- USE ----------------
    // Methods simulate printing, reducing paper and ink by one unit per use
    // Encapsulation: internal state changes are controlled
    public void usePaper() { if (paperLevel > 0) paperLevel--; }
    public void useInk() { if (inkLevel > 0) inkLevel--; }

    // ---------------- SETTERS (for technician use) ----------------
    // Allows refilling printer resources
    // Encapsulation: protects against negative values
    public void setPaperLevel(int paperLevel) {
        if (paperLevel < 0) paperLevel = 0;
        this.paperLevel = paperLevel;
    }

    public void setInkLevel(int inkLevel) {
        if (inkLevel < 0) inkLevel = 0;
        this.inkLevel = inkLevel;
    }
}
