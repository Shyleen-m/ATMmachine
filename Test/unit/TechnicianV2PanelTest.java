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

// [SOLID - SRP] This class has one responsibility: testing the Technician's maintenance logic.
public class TechnicianV2PanelTest {

    private ATMMachineV2 atm;
    private PrinterService printer;

    // [Logic - Setup] This method prepares the "hardware" environment before each test case.
    @BeforeEach
    void setup() {
        printer = new PrinterService(0, 0); // Start empty

        // [SOLID - DIP] Dependency Inversion: We use the Interface to create a "Mock" service.
        // [Speaker Note] This allows testing without actually reading or writing to your JSON files.
        IATMStateService mockService = new IATMStateService() {
            public void saveState(List<Account> a, double c, int p, int i, String f) {}
            public List<Account> loadAccounts() { return new ArrayList<>(); }
            public int loadPaperLevel() { return 0; }
            public int loadInkLevel() { return 0; }
            public double loadCashLevel() { return 100.0; }
            public String loadFirmwareVersion() { return "1.0.0"; }
        };

        // [OOP - Composition] Injecting the mock dependencies into the ATM machine.
        atm = new ATMMachineV2(mockService, printer);
    }

    // [Logic - Verification] Testing the refill system to ensure levels update correctly.
    @Test
    void testRefillPaperAndInk() {
        atm.refillPaper(50);
        atm.refillInk(20);

        // [Logic - Assertions] Confirming the internal state matches the refill amounts.
        assertEquals(50, atm.getPaperAvailable());
        assertEquals(20, atm.getInkAvailable());
        // [Logic - State Check] Ensures the machine recognizes it's now back in service.
        assertFalse(atm.isOutOfService(), "ATM should be in service after refill");
    }

    // [Logic - Arithmetic] Verifying the math for adding and removing cash from the vault.
    @Test
    void testCashOperations() {
        double initial = atm.getCashAvailable(); // 100.0
        atm.refillCash(500);
        assertEquals(600.0, atm.getCashAvailable());

        atm.collectCash(200);
        assertEquals(400.0, atm.getCashAvailable());
    }

    // [Logic - String Update] Testing the ability to update the ATM's system version.
    @Test
    void testFirmwareUpdate() {
        // Proves the machine can handle software version changes.
        atm.updateFirmware("2.0.5");
        assertEquals("2.0.5", atm.getFirmwareVersion());
    }
}