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

class ATMUserFlowTest {

    private ATMMachineV2 atm;
    private PrinterService printer;
    private PersistenceService persistence;

    @BeforeEach
    void setup() {
        printer = new PrinterService(5, 5);
        persistence = new PersistenceService();

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

        atm = new ATMMachineV2(stateService, printer);
    }

    @Test
    void testDepositAndWithdraw() {
        System.out.println("--- User Flow: Deposit and Withdraw ---");

        Account acc = atm.authenticateUser("Charlie", "0000");
        assertNotNull(acc);

        atm.deposit("Charlie", 100); // prints: Successfully deposited
        assertEquals(100, acc.getBalance());

        boolean success = atm.withdraw("Charlie", 50); // prints: Desired amount reached + Receipt
        assertTrue(success);
        assertEquals(50, acc.getBalance());
    }

    @Test
    void testLowPaperOrInkPreventsTransaction() {
        System.out.println("--- User Flow: Low Paper/Ink ---");

        printer.setPaperLevel(1);
        printer.setInkLevel(1);

        Account acc = atm.authenticateUser("Dana", "1111");
        assertNotNull(acc);

        // simulate transaction that will fail because paper/ink will deplete
        printer.setPaperLevel(0);
        boolean success = atm.withdraw("Dana", 10); // prints warning and out-of-service
        assertFalse(success);

        printer.setInkLevel(0);
        success = atm.withdraw("Dana", 10); // prints warning and out-of-service
        assertFalse(success);
    }
}
