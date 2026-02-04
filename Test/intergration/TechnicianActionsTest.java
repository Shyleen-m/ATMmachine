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

// [SOLID - SRP] This class handles Integration Testing, ensuring different services work together.
class TechnicianActionsTest {

    private ATMMachineV2 atm;
    private PrinterService printer;
    private PersistenceService persistence;

    // [Logic - Setup] Rebuilding the environment before each test for consistency.
    @BeforeEach
    void setup() {
        // [OOP - Composition] Initializing the real Printer and Persistence services.
        printer = new PrinterService(10, 10);
        persistence = new PersistenceService();

        /**
         * [SOLID - DIP] Dependency Inversion: Creating an anonymous implementation of the Interface.
         * [Speaker Note] This bridges our real PersistenceService with the ATM's interface requirements.
         */
        IATMStateService stateService = new IATMStateService() {
            @Override
            public List<Account> loadAccounts() { return persistence.loadAccounts(); }
            @Override
            public double loadCashLevel() { return 500; }
            @Override
            public int loadPaperLevel() { return 10; }
            @Override
            public int loadInkLevel() { return 10; }
            @Override
            public String loadFirmwareVersion() { return "1.0.0"; }
            @Override
            public void saveState(List<Account> accounts, double cash, int paper, int ink, String firmware) {
                // [Logic - Delegation] Passing data to the real persistence layer.
                persistence.saveState(accounts, cash, paper, ink);
            }
        };

        // [OOP - Dependency Injection] Injecting our setup into the ATM core.
        atm = new ATMMachineV2(stateService, printer);
    }

    // [Logic - Integration Test] Verifying the ATM and Persistence layer agree on cash levels.
    @Test
    void testCashRefillAndCollect() {
        System.out.println("--- Testing Cash Refill and Collect ---");
        double beforeCash = atm.getCashAvailable();
        atm.refillCash(200); // should print [+] Cash refilled
        // [Logic - Assertions] Validating that the mathematical state updated correctly.
        assertEquals(beforeCash + 200, atm.getCashAvailable());

        atm.collectCash(150); // should print [+] Cash collected
        assertEquals(beforeCash + 50, atm.getCashAvailable());
    }

    // [Logic - Hardware Simulation] Testing the interaction between ATM core and PrinterService.
    @Test
    void testPaperAndInkRefill() {
        System.out.println("--- Testing Paper and Ink Refill ---");
        // [OOP - Encapsulation] Modifying printer state through defined methods.
        printer.setPaperLevel(2);
        printer.setInkLevel(1);

        atm.refillPaper(5); // should print [+] Paper refilled
        atm.refillInk(4);    // should print [+] Ink refilled

        assertEquals(7, atm.getPaperAvailable());
        assertEquals(5, atm.getInkAvailable());
    }

    // [Logic - Business Logic] Testing the validation rules for software updates.
    @Test
    void testFirmwareUpdate() {
        System.out.println("--- Testing Firmware Update ---");
        atm.updateFirmware("1.1.1"); // should print [+] Firmware updated
        assertEquals("1.1.1", atm.getFirmwareVersion());

        // [Logic - Error Case] Ensuring the version does NOT change if the input is invalid.
        atm.updateFirmware("invalid"); // should print invalid message, version unchanged
        assertEquals("1.1.1", atm.getFirmwareVersion());
    }
}