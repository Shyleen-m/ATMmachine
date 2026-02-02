package interfaces;

// Abstraction (OOP):
// Defines actions available to a technician without specifying how they are implemented
// SOLID - Interface Segregation Principle (ISP):
// This interface contains only technician-specific operations, keeping it focused on the tech role
public interface ITechActions {

    // Abstraction of ATM status viewing
    // Encapsulation: Hides internal ATM details; implementation decides what data to display
    // Liskov Substitution Principle (LSP):
    // Any class implementing this interface must provide a consistent view of ATM status
    void viewATMStatus();
}
