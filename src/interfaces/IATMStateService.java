package interfaces;

import model.Account;
import java.util.List;

// Abstraction (OOP): Defines WHAT the ATM needs for persistence, not HOW it is done
// SOLID - Dependency Inversion Principle (DIP):
// High-level modules (ATMMachineV2) depend on this interface, not on concrete storage classes
// SOLID - Liskov Substitution Principle (LSP):
// Any class implementing this interface must behave consistently so it can replace another implementation safely
public interface IATMStateService {

    // Single Responsibility Principle (SRP):
    // This method is responsible only for saving ATM system state
    // Encapsulates all persistent ATM data in one operation
    void saveState(List<Account> accounts, double cash, int paper, int ink, String firmware);

    // Abstraction: ATM does not know where accounts come from (file, DB, cloud, etc.)
    // LSP: Any implementation must return a valid list of accounts
    List<Account> loadAccounts();

    // Encapsulation of printer resource persistence
    // SRP: Each method retrieves only one piece of system state
    int loadPaperLevel();

    int loadInkLevel();

    // Encapsulation of ATM internal cash state
    double loadCashLevel();

    // Abstraction of firmware version storage
    String loadFirmwareVersion();
}
