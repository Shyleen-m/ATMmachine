package unit;

import org.junit.jupiter.api.Test;
import services.PrinterService;
import static org.junit.jupiter.api.Assertions.*;

public class PrinterServiceTest {

    @Test
    void testPrinterUse() {
        System.out.println("--- PrinterService Test: Use Paper and Ink ---");

        PrinterService printer = new PrinterService(2, 2);
        System.out.println("Initial paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        assertTrue(printer.hasPaper(), "Printer should initially have paper");
        assertTrue(printer.hasInk(), "Printer should initially have ink");

        printer.usePaper();
        printer.useInk();
        System.out.println("After 1 use - paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        assertEquals(1, printer.getPaperLevel(), "Paper should decrease by 1");
        assertEquals(1, printer.getInkLevel(), "Ink should decrease by 1");

        printer.usePaper();
        printer.useInk();
        System.out.println("After 2nd use - paper: " + printer.getPaperLevel() + ", ink: " + printer.getInkLevel());

        // Should reach zero
        assertEquals(0, printer.getPaperLevel(), "Paper should be zero now");
        assertEquals(0, printer.getInkLevel(), "Ink should be zero now");

        assertFalse(printer.hasPaper(), "Printer should have no paper left");
        assertFalse(printer.hasInk(), "Printer should have no ink left");

        System.out.println("PrinterService test completed ✅");
    }
}
