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

class TechnicianActionsTest {

    private ATMMachineV2 atm;
    private PrinterService printer;
    private PersistenceService persistence;

    @BeforeEach
    void setup() {
        printer = new PrinterService(10, 10);
        persistence = new PersistenceService();

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
                persistence.saveState(accounts, cash, paper, ink);
            }
        };

        atm = new ATMMachineV2(stateService, printer);
    }

    @Test
    void testCashRefillAndCollect() {
        System.out.println("--- Testing Cash Refill and Collect ---");
        double beforeCash = atm.getCashAvailable();
        atm.refillCash(200); // should print [+] Cash refilled
        assertEquals(beforeCash + 200, atm.getCashAvailable());

        atm.collectCash(150); // should print [+] Cash collected
        assertEquals(beforeCash + 50, atm.getCashAvailable());
    }

    @Test
    void testPaperAndInkRefill() {
        System.out.println("--- Testing Paper and Ink Refill ---");
        printer.setPaperLevel(2);
        printer.setInkLevel(1);

        atm.refillPaper(5); // should print [+] Paper refilled
        atm.refillInk(4);    // should print [+] Ink refilled

        assertEquals(7, atm.getPaperAvailable());
        assertEquals(5, atm.getInkAvailable());
    }

    @Test
    void testFirmwareUpdate() {
        System.out.println("--- Testing Firmware Update ---");
        atm.updateFirmware("1.1.1"); // should print [+] Firmware updated
        assertEquals("1.1.1", atm.getFirmwareVersion());

        atm.updateFirmware("invalid"); // should print invalid message, version unchanged
        assertEquals("1.1.1", atm.getFirmwareVersion());
    }
}
