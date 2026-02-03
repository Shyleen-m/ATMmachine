package unit;

import model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import services.FileATMStateService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileATMStateServiceTest {

    private final String PATH = "data/atm_state.json";
    private FileATMStateService service = new FileATMStateService();

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(PATH));
    }

    @Test
    void testSaveProtectsNgaaAccount() {
        List<Account> accounts = new ArrayList<>();
        // Save empty list (attempting to delete everyone)
        service.saveState(accounts, 1000, 10, 10, "1.0");

        // Reload
        List<Account> loaded = service.loadAccounts();

        // Assert 'ngaa' was auto-added
        boolean hasNgaa = loaded.stream().anyMatch(a -> a.getOwner().equals("ngaa"));
        assertTrue(hasNgaa, "PersistenceService must auto-create 'ngaa' if missing during save");
    }
}