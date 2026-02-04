package intergration;

import core.ATMMachineV2;
import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.PrinterService;
import services.PersistenceService;
import interfaces.IATMStateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// [SOLID - SRP] This class focuses on testing the end-to-end "Happy Path" and "Failure Path" for customers.
class ATMUserFlowTest {

    private ATMMachineV2 atm;
    private PrinterService printer;
    private PersistenceService persistence;

    // [Logic - Setup] Initializing the integrated components (Core, Printer, Persistence).
    @BeforeEach
    void setup() {
        printer = new PrinterService(5, 5);
        persistence = new PersistenceService();

        /**
         * [SOLID - DIP] Dependency Inversion: Connecting the persistence logic to the ATM through the Interface.
         * [Speaker Note] This ensures that user data and hardware levels are consistent throughout the test.
         */
        IATMStateService stateService = new IATMStateService() {
            @Override
            public List<Account> loadAccounts() { return persistence.loadAccounts();}
            @Override
            public double loadCashLevel() { return 1000; }
            @Override
            public int loadPaperLevel() { return persistence.loadPaperLevel(); }
            @Override
            public int loadInkLevel() { return persistence.loadInkLevel(); }
            @Override
            public String loadFirmwareVersion() { return "1.0.0"; }
            @Override
            public void saveState(List<Account> accounts, double cash, int paper, int ink, String firmware) {
                persistence.saveState(accounts, cash, paper, ink);
            }
        };

        // [OOP - Dependency Injection] Plugging the services into the ATM engine.
        atm = new ATMMachineV2(stateService, printer);

        // [Update - Pre-condition] Ensure ATM has resources so authentication doesn't return null
        atm.refillPaper(100);
        atm.refillInk(100);
        atm.refillCash(1000);
    }

    // [Logic - Happy Path Test] Verifying that a standard user session works as expected.
    @Test
    void testDepositAndWithdraw() {
        System.out.println("--- User Flow: Deposit and Withdraw ---");

        // [Logic - Authentication] Simulating a user logging in.
        Account acc = atm.authenticateUser("Charlie", "0000");
        assertNotNull(acc, "User should not be null if hardware is ready");

        // [Logic - Balance Mutation] testing that the account and ATM agree on the new balance.
        atm.deposit("Charlie", 100); // prints: Successfully deposited
        assertEquals(100, acc.getBalance());

        boolean success = atm.withdraw("Charlie", 50); // prints: Desired amount reached + Receipt
        assertTrue(success);
        assertEquals(50, acc.getBalance());
    }

    // [Logic - Failure Path Test] Testing the "Fail-Safe" behavior of the hardware.
    @Test
    void testLowPaperOrInkPreventsTransaction() {
        System.out.println("--- User Flow: Low Paper/Ink ---");

        // [Logic - State Setup] Manually setting levels to near-depletion.
        printer.setPaperLevel(1);
        printer.setInkLevel(1);

        Account acc = atm.authenticateUser("Dana", "1111");
        assertNotNull(acc);

        // [Logic - Boundary Test] Forcing the printer to zero to trigger the safety block.
        // simulate transaction that will fail because paper/ink will deplete
        printer.setPaperLevel(0);
        boolean success = atm.withdraw("Dana", 10); // prints warning and out-of-service
        assertFalse(success, "Withdrawal should fail when paper is 0");

        printer.setInkLevel(0);
        success = atm.withdraw("Dana", 10); // prints warning and out-of-service
        assertFalse(success, "Withdrawal should fail when ink is 0");
    }
}