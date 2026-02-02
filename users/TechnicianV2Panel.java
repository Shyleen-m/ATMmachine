package users;

import core.ATMMachineV2;
import java.util.Scanner;

public class TechnicianV2Panel {

    private final ATMMachineV2 atm;

    public TechnicianV2Panel(ATMMachineV2 atm) {
        this.atm = atm;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- TECHNICIAN PANEL ---");
            System.out.println("1. View ATM Status");
            System.out.println("2. Refill Cash");
            System.out.println("3. Collect Cash");
            System.out.println("4. Refill Paper");
            System.out.println("5. Refill Ink");
            System.out.println("6. Update Firmware");
            System.out.println("0. Logout");
            System.out.print("Select: ");

            int choice;
            try { choice = Integer.parseInt(sc.nextLine()); }
            catch (Exception e) { System.out.println("Invalid input."); continue; }

            switch (choice) {
                case 1 -> atm.viewATMStatus();
                case 2 -> {
                    System.out.print("Enter cash amount to refill: ");
                    int amount = Integer.parseInt(sc.nextLine());
                    atm.refillCash(amount);
                }
                case 3 -> {
                    System.out.print("Enter cash amount to collect: ");
                    int amount = Integer.parseInt(sc.nextLine());
                    atm.collectCash(amount);
                }
                case 4 -> {
                    System.out.print("Enter sheets to refill: ");
                    int sheets = Integer.parseInt(sc.nextLine());
                    atm.refillPaper(sheets);
                }
                case 5 -> {
                    System.out.print("Enter ink units to refill: ");
                    int units = Integer.parseInt(sc.nextLine());
                    atm.refillInk(units);
                }
                case 6 -> {
                    System.out.print("Enter new firmware version: ");
                    String version = sc.nextLine();
                    atm.updateFirmware(version);
                }
                case 0 -> {
                    System.out.println("Logging out...");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }
}
