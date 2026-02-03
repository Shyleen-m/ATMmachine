package unit;

import core.ATMMachineV2;
import interfaces.IATMStateService;
import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.PrinterService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TechnicianV2PanelTest {

    private ATMMachineV2 atm;
    private PrinterService printer;

    @BeforeEach
    void setup() {
        printer = new PrinterService(0, 0); // Start empty

        // Mock persistence to avoid writing actual files during test
        IATMStateService mockService = new IATMStateService() {
            public void saveState(List<Account> a, double c, int p, int i, String f) {}
            public List<Account> loadAccounts() { return new ArrayList<>(); }
            public int loadPaperLevel() { return 0; }
            public int loadInkLevel() { return 0; }
            public double loadCashLevel() { return 100.0; }
            public String loadFirmwareVersion() { return "1.0.0"; }
        };

        atm = new ATMMachineV2(mockService, printer);
    }

    @Test
    void testRefillPaperAndInk() {
        atm.refillPaper(50);
        atm.refillInk(20);

        assertEquals(50, atm.getPaperAvailable());
        assertEquals(20, atm.getInkAvailable());
        assertFalse(atm.isOutOfService(), "ATM should be in service after refill");
    }

    @Test
    void testCashOperations() {
        double initial = atm.getCashAvailable(); // 100.0
        atm.refillCash(500);
        assertEquals(600.0, atm.getCashAvailable());

        atm.collectCash(200);
        assertEquals(400.0, atm.getCashAvailable());
    }

    @Test
    void testFirmwareUpdate() {
        atm.updateFirmware("2.0.5");
        assertEquals("2.0.5", atm.getFirmwareVersion());
    }
}