package interfaces;

// Abstraction (OOP):
// Defines the set of actions available to ATM customers without specifying how they are implemented
// SOLID - Interface Segregation Principle (ISP):
// This interface contains only customer-related operations, keeping it focused and role-specific
public interface ICustomerActions {

    // Encapsulation:
    // Customer balance is accessed through a method, not by exposing the Account object directly
    // Abstraction: The caller does not need to know how the balance is retrieved
    double checkBalance(String name);

    // Abstraction of deposit behavior
    // SOLID - Single Responsibility Principle (SRP):
    // The ATM class implementing this interface handles transaction logic, while storage is delegated elsewhere
    void deposit(String name, int amount);

    // Abstraction of withdrawal behavior
    // Liskov Substitution Principle (LSP):
    // Any class implementing this interface must support withdrawals in a way that respects expected ATM rules
    boolean withdraw(String name, int amount);

    // Abstraction of receipt printing
    // Interface Segregation Principle (ISP):
    // Only customer-related printing is included here, not maintenance printing
    void printReceipt();
}
