package unit;

import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AccountTest {

    private Account account;

    // ----------------------- SETUP -----------------------
    // This method runs before each test. We create a fresh Account object for consistency.
    @BeforeEach
    public void setup() {
        account = new Account("Alice", "1234", 30.0);
        System.out.println("\n--- Setup: Created Account for Alice with balance €30.00 ---");
    }

    // ------------------- TEST DEPOSIT TRANSACTION -------------------
    @Test
    public void testAddTransaction_Deposit() {
        System.out.println("\n[Test] Deposit Transaction");

        // Simulate a deposit by increasing the balance
        account.setBalance(account.getBalance() + 20);
        System.out.println("Deposited €20. New balance: €" + account.getBalance());

        // Record the deposit in the transactions list
        account.addTransaction("Deposit", 20);

        // Fetch the transactions for assertions
        List<String> txns = account.getTransactions();
        System.out.println("Transactions: " + txns);

        // Check that exactly one transaction was recorded
        assertEquals(1, txns.size());

        // Get the actual transaction string
        String entry = txns.get(0);

        // Check that the transaction string contains the correct amount
        assertTrue(entry.contains("Deposit: €20.00"), "Transaction should include type and amount");

        // Check that the transaction string contains the updated balance
        assertTrue(entry.contains("Balance: €50.00"), "Transaction should include new balance");

        System.out.println("[✅] Deposit transaction test passed.");
    }

    // ------------------- TEST WITHDRAW TRANSACTION -------------------
    @Test
    public void testAddTransaction_Withdraw() {
        System.out.println("\n[Test] Withdraw Transaction");

        // Simulate a withdrawal by reducing the balance
        account.setBalance(account.getBalance() - 10);
        System.out.println("Withdrew €10. New balance: €" + account.getBalance());

        // Record the withdrawal in the transactions list
        account.addTransaction("Withdraw", 10);

        // Fetch the transactions for assertions
        List<String> txns = account.getTransactions();
        System.out.println("Transactions: " + txns);

        // Check that exactly one transaction was recorded
        assertEquals(1, txns.size());

        // Get the actual transaction string
        String entry = txns.get(0);

        // Check that the transaction string contains the correct amount
        assertTrue(entry.contains("Withdraw: €10.00"), "Transaction should include type and amount");

        // Check that the transaction string contains the updated balance
        assertTrue(entry.contains("Balance: €20.00"), "Transaction should include new balance");

        System.out.println("[✅] Withdraw transaction test passed.");
    }
}
