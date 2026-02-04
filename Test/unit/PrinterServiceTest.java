package unit;

import org.junit.jupiter.api.Test;
import services.PrinterService;
import static org.junit.jupiter.api.Assertions.*;

// [SOLID - SRP] This test class is dedicated solely to verifying the PrinterService logic.
public class PrinterServiceTest {

    // [Logic - Test Case] Simulates the lifecycle of a printer from full to empty.
    @Test
    void testPrinterUse() {
        System.out.println("--- PrinterService Test: Use Paper and Ink ---");

        // [Logic - Initialization] Setting up a printer with limited resources (2 units each).
        PrinterService printer = new PrinterService(2, 2);
        System.out.println("Initial paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        // [Logic - Assertions] Verifying the initial state of the hardware.
        assertTrue(printer.hasPaper(), "Printer should initially have paper");
        assertTrue(printer.hasInk(), "Printer should initially have ink");

        // [Logic - State Mutation] Simulating the action of printing one receipt.
        printer.usePaper();
        printer.useInk();
        System.out.println("After 1 use - paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        // [Logic - Verification] Ensuring the decrement logic correctly subtracts exactly 1.
        assertEquals(1, printer.getPaperLevel(), "Paper should decrease by 1");
        assertEquals(1, printer.getInkLevel(), "Ink should decrease by 1");

        // [Logic - Boundary Testing] Using the remaining resources to reach the limit.
        printer.usePaper();
        printer.useInk();
        System.out.println("After 2nd use - paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        // [Logic - Zero-Check] Validating that the system correctly identifies when resources are depleted.
        // Should reach zero
        assertEquals(0, printer.getPaperLevel(), "Paper should be zero now");
        assertEquals(0, printer.getInkLevel(), "Ink should be zero now");

        // [Logic - Final Verification] Confirming the boolean flags update when levels hit 0.
        assertFalse(printer.hasPaper(), "Printer should have no paper left");
        assertFalse(printer.hasInk(), "Printer should have no ink left");

        System.out.println("PrinterService test completed ✅");
    }
}