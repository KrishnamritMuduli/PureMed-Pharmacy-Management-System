package com.pharmacy.cli;

import com.pharmacy.dto.CartItemRequest;
import com.pharmacy.dto.CheckoutRequest;
import com.pharmacy.dto.CreditActionRequest;
import com.pharmacy.dto.InvoiceReceipt;
import com.pharmacy.model.*;
import com.pharmacy.repository.*;
import com.pharmacy.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Pure Java Terminal / Console Runner for the Pharmacy Management System.
 * Can be executed directly or alongside the web application.
 */
public class StandaloneConsoleApp {

    public static void main(String[] args) {
        // Initialize in-memory repositories
        SellerRepository sellerRepo = new SellerRepository();
        MedicineRepository medicineRepo = new MedicineRepository();
        BorrowerRepository borrowerRepo = new BorrowerRepository();
        CreditTransactionRepository creditTxRepo = new CreditTransactionRepository();
        SaleRepository saleRepo = new SaleRepository();

        // Initialize services
        AuthService authService = new AuthService(sellerRepo);
        InventoryService inventoryService = new InventoryService(medicineRepo);
        BorrowerService borrowerService = new BorrowerService(borrowerRepo, creditTxRepo);
        BillingService billingService = new BillingService(medicineRepo, saleRepo, borrowerService);
        AnalyticsService analyticsService = new AnalyticsService(saleRepo, medicineRepo, borrowerService);

        // Preload sample data
        preloadData(medicineRepo, borrowerRepo, creditTxRepo, saleRepo);

        Scanner scanner = new Scanner(System.in);
        System.out.println("===============================================================");
        System.out.println("   WELCOME TO PURE JAVA PHARMACY MANAGEMENT SYSTEM (CLI)");
        System.out.println("   Zero external database setup - In-memory thread-safe store");
        System.out.println("===============================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n----------------- MAIN MENU -----------------");
            System.out.println("1. View Medicine Inventory & Stock Alerts");
            System.out.println("2. Add New Medicine / Batch Purchase");
            System.out.println("3. Dispense Medicine / Point of Sale (POS)");
            System.out.println("4. Customer Khata (Credit Ledger) & Repayments");
            System.out.println("5. Profit & Loss (P&L) Report");
            System.out.println("6. Next-Month Restock Estimator (Pure Java Heuristic)");
            System.out.println("7. Exit CLI");
            System.out.print("Select an option (1-7): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showInventory(inventoryService);
                case "2" -> addOrRestockMedicine(inventoryService, scanner);
                case "3" -> dispenseMedicine(billingService, inventoryService, borrowerService, scanner);
                case "4" -> manageKhata(borrowerService, scanner);
                case "5" -> showProfitLoss(analyticsService);
                case "6" -> showRestockEstimator(analyticsService);
                case "7" -> {
                    System.out.println("Exiting Pharmacy CLI. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option! Please enter a number between 1 and 7.");
            }
        }
    }

    private static void showInventory(InventoryService inventoryService) {
        List<Medicine> medicines = inventoryService.getAllMedicines();
        System.out.println("\n--- CURRENT MEDICINE INVENTORY (" + medicines.size() + " items) ---");
        System.out.printf("%-10s %-24s %-12s %-8s %-10s %-10s %-12s %-10s\n",
                "ID", "Name", "Batch", "Stock", "Buy($)", "Sell($)", "Expiry", "Status");
        System.out.println("--------------------------------------------------------------------------------------------------");

        for (Medicine m : medicines) {
            String status = "OK";
            if (m.isExpired()) {
                status = "EXPIRED!";
            } else if (m.isLowStock()) {
                status = "LOW STOCK";
            } else if (m.isNearExpiry(60)) {
                status = "EXPIRING";
            }

            System.out.printf("%-10s %-24s %-12s %-8d $%-9.2f $%-9.2f %-12s %-10s\n",
                    m.getId(),
                    truncate(m.getName(), 23),
                    m.getBatchNumber() != null ? m.getBatchNumber() : "-",
                    m.getQuantityInStock(),
                    m.getPurchasePrice(),
                    m.getSellingPrice(),
                    m.getExpiryDate() != null ? m.getExpiryDate() : "-",
                    status);
        }
    }

    private static void addOrRestockMedicine(InventoryService inventoryService, Scanner scanner) {
        System.out.print("\nAdd [N]ew medicine or [R]estock existing batch? (n/r): ");
        String sub = scanner.nextLine().trim().toLowerCase();
        if ("r".equals(sub)) {
            System.out.print("Enter Medicine ID: ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter Quantity to Add: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            com.pharmacy.dto.BatchRestockRequest req = new com.pharmacy.dto.BatchRestockRequest();
            req.setQuantityToAdd(qty);
            try {
                Medicine updated = inventoryService.restockBatch(id, req);
                System.out.println("Successfully restocked! New stock level for " + updated.getName() + " is " + updated.getQuantityInStock());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.print("Medicine Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Generic Name: ");
            String generic = scanner.nextLine().trim();
            System.out.print("Batch Number: ");
            String batch = scanner.nextLine().trim();
            System.out.print("Initial Stock: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Purchase Price ($): ");
            BigDecimal buy = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Selling Price ($): ");
            BigDecimal sell = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Expiry Date (YYYY-MM-DD): ");
            LocalDate expiry = LocalDate.parse(scanner.nextLine().trim());

            Medicine m = new Medicine(null, name, generic, batch, stock, buy, sell, expiry, "General", "Standard Pharma");
            Medicine created = inventoryService.addMedicine(m);
            System.out.println("Medicine created successfully with ID: " + created.getId());
        }
    }

    private static void dispenseMedicine(BillingService billingService, InventoryService inventoryService,
                                         BorrowerService borrowerService, Scanner scanner) {
        System.out.println("\n--- POINT OF SALE (DISPENSING) ---");
        System.out.print("Enter Medicine ID to dispense: ");
        String medId = scanner.nextLine().trim();
        System.out.print("Quantity to sell: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Payment mode ([1] CASH, [2] UPI, [3] CREDIT / KHATA): ");
        String modeChoice = scanner.nextLine().trim();
        String mode = "CASH";
        String borrowerId = null;

        if ("2".equals(modeChoice)) {
            mode = "UPI";
        } else if ("3".equals(modeChoice)) {
            mode = "CREDIT";
            System.out.println("Existing borrowers:");
            for (Borrower b : borrowerService.getAllBorrowers()) {
                System.out.println(" - [" + b.getId() + "] " + b.getCustomerName() + " (Debt: $" + b.getOutstandingBalance() + ")");
            }
            System.out.print("Enter Borrower ID: ");
            borrowerId = scanner.nextLine().trim();
        }

        CheckoutRequest req = new CheckoutRequest(
                List.of(new CartItemRequest(medId, qty)),
                mode,
                borrowerId,
                "Customer"
        );

        try {
            InvoiceReceipt receipt = billingService.checkout(req);
            System.out.println("Checkout successful!");
            System.out.println("Invoice #: " + receipt.getInvoiceNumber());
            System.out.println("Total Amount: $" + receipt.getGrandTotal());
            System.out.println("Payment: " + receipt.getPaymentMode());
            if (receipt.getBorrowerNewBalance() != null) {
                System.out.println("Updated Borrower Balance: $" + receipt.getBorrowerNewBalance());
            }
        } catch (Exception e) {
            System.out.println("Checkout failed: " + e.getMessage());
        }
    }

    private static void manageKhata(BorrowerService borrowerService, Scanner scanner) {
        System.out.println("\n--- CUSTOMER BORROWER & CREDIT LEDGER (KHATA) ---");
        List<Borrower> borrowers = borrowerService.getAllBorrowers();
        for (Borrower b : borrowers) {
            System.out.printf("[%s] %-18s | Phone: %-15s | Debt: $%-8.2f\n",
                    b.getId(), b.getCustomerName(), b.getPhoneNumber(), b.getOutstandingBalance());
        }
        System.out.println("Total Store Outstanding Debt: $" + borrowerService.getTotalOutstandingDebt());

        System.out.println("\nOptions: [1] View Customer Ledger  [2] Record Cash/UPI Repayment  [3] Back");
        System.out.print("Selection: ");
        String sub = scanner.nextLine().trim();
        if ("1".equals(sub)) {
            System.out.print("Enter Borrower ID: ");
            String id = scanner.nextLine().trim();
            List<CreditTransaction> ledger = borrowerService.getLedger(id);
            System.out.println("\n--- LEDGER TRANSACTIONS FOR " + id + " ---");
            for (CreditTransaction tx : ledger) {
                System.out.printf("%s | Type: %-13s | Amount: $%-7.2f | Balance: $%-7.2f | Note: %s\n",
                        tx.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getRunningBalance(),
                        tx.getNote());
            }
        } else if ("2".equals(sub)) {
            System.out.print("Enter Borrower ID: ");
            String id = scanner.nextLine().trim();
            System.out.print("Repayment Amount ($): ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Payment Method (CASH/UPI): ");
            String method = scanner.nextLine().trim().toUpperCase();

            CreditActionRequest req = new CreditActionRequest(amount, "Customer repayment", method);
            CreditTransaction tx = borrowerService.recordPayment(id, req);
            System.out.println("Repayment recorded! New outstanding balance: $" + tx.getRunningBalance());
        }
    }

    private static void showProfitLoss(AnalyticsService analyticsService) {
        ProfitLossSummary pnl = analyticsService.calculateProfitLoss(null);
        System.out.println("\n=======================================================");
        System.out.println("              PROFIT & LOSS (P&L) STATEMENT           ");
        System.out.println("=======================================================");
        System.out.printf(" Total Revenue (Sales)    : $%.2f\n", pnl.getTotalRevenue());
        System.out.printf(" Cost of Goods Sold (COGS): $%.2f\n", pnl.getTotalCogs());
        System.out.println(" ------------------------------------------------------");
        System.out.printf(" Gross Profit             : $%.2f\n", pnl.getGrossProfit());
        System.out.printf(" Gross Margin %%           : %.2f%%\n", pnl.getGrossMarginPercentage());
        System.out.println(" ------------------------------------------------------");
        System.out.printf(" Pending Receivables (Khata): $%.2f\n", pnl.getPendingReceivables());
        System.out.printf(" Total Inventory Cost     : $%.2f\n", pnl.getInventoryValueAtCost());
        System.out.printf(" Total Inventory Retail   : $%.2f\n", pnl.getInventoryValueAtRetail());
        System.out.printf(" Total Units Sold         : %d units across %d transactions\n",
                pnl.getTotalUnitsSold(), pnl.getTotalTransactions());
        System.out.println("=======================================================");
    }

    private static void showRestockEstimator(AnalyticsService analyticsService) {
        List<RestockRecommendation> recs = analyticsService.calculateRestockEstimates(30);
        System.out.println("\n==========================================================================================");
        System.out.println("              NEXT-MONTH RESTOCK ESTIMATOR (30-DAY SALES RUN RATE)                      ");
        System.out.println("  Formula: Recommended = max(0, Projected Demand(30d) + 20% Buffer - Current Stock)      ");
        System.out.println("==========================================================================================");
        System.out.printf("%-18s %-7s %-9s %-10s %-8s %-12s %-11s %-12s\n",
                "Medicine", "Stock", "Sold(30d)", "Avg/Day", "Buffer", "Recommended", "Unit Buy", "Reorder Cost");
        System.out.println("------------------------------------------------------------------------------------------");

        for (RestockRecommendation r : recs) {
            System.out.printf("%-18s %-7d %-9d %-10.2f %-8.1f %-12d $%-10.2f $%-11.2f\n",
                    truncate(r.getMedicineName(), 17),
                    r.getCurrentStock(),
                    r.getTotalSoldInPeriod(),
                    r.getAvgDailySales(),
                    r.getBufferStock(),
                    r.getRecommendedPurchaseQty(),
                    r.getUnitPurchasePrice(),
                    r.getEstimatedReorderCost());
        }
        System.out.println("==========================================================================================");
    }

    private static String truncate(String val, int max) {
        if (val == null) return "";
        return val.length() > max ? val.substring(0, max - 1) + "…" : val;
    }

    private static void preloadData(MedicineRepository medRepo, BorrowerRepository borRepo,
                                    CreditTransactionRepository txRepo, SaleRepository saleRepo) {
        Medicine m1 = new Medicine("MED-PCM500", "Paracetamol 500mg", "Acetaminophen", "BAT-2026-A1", 120,
                new BigDecimal("1.50"), new BigDecimal("3.00"), LocalDate.now().plusMonths(18), "Analgesic", "Cipla");
        Medicine m2 = new Medicine("MED-AMX500", "Amoxicillin 500mg", "Amoxicillin Trihydrate", "BAT-2026-B4", 45,
                new BigDecimal("4.20"), new BigDecimal("8.50"), LocalDate.now().plusMonths(12), "Antibiotic", "Sun Pharma");
        Medicine m3 = new Medicine("MED-CTZ010", "Cetirizine 10mg", "Cetirizine HCl", "BAT-2026-E5", 8,
                new BigDecimal("0.80"), new BigDecimal("2.50"), LocalDate.now().plusMonths(8), "Antihistamine", "Torrent");
        Medicine m4 = new Medicine("MED-IBU400", "Ibuprofen 400mg", "Ibuprofen BP", "BAT-2026-H1", 0,
                new BigDecimal("1.80"), new BigDecimal("4.00"), LocalDate.now().plusMonths(9), "Anti-inflammatory", "Zydus");

        medRepo.save(m1);
        medRepo.save(m2);
        medRepo.save(m3);
        medRepo.save(m4);

        Borrower b1 = new Borrower("BOR-1001", "Rajesh Kumar", "+91 98111 22233", "rajesh@gmail.com", "Main Rd", new BigDecimal("45.00"));
        Borrower b2 = new Borrower("BOR-1002", "Priya Sharma", "+91 98444 55566", "priya@yahoo.com", "Park Ave", new BigDecimal("24.00"));
        borRepo.save(b1);
        borRepo.save(b2);

        txRepo.save(new CreditTransaction("TX-101", b1.getId(), new BigDecimal("45.00"), CreditTransaction.Type.CREDIT_TAKEN,
                java.time.LocalDateTime.now().minusDays(5), "Initial balance", "CREDIT", new BigDecimal("45.00")));
        txRepo.save(new CreditTransaction("TX-102", b2.getId(), new BigDecimal("24.00"), CreditTransaction.Type.CREDIT_TAKEN,
                java.time.LocalDateTime.now().minusDays(3), "Initial balance", "CREDIT", new BigDecimal("24.00")));

        saleRepo.save(new Sale("S1", "INV-101", m1.getId(), m1.getName(), 15, m1.getPurchasePrice(), m1.getSellingPrice(),
                java.time.LocalDateTime.now().minusDays(4), "CASH", null, "Walk-in"));
        saleRepo.save(new Sale("S2", "INV-102", m2.getId(), m2.getName(), 6, m2.getPurchasePrice(), m2.getSellingPrice(),
                java.time.LocalDateTime.now().minusDays(2), "UPI", null, "Anita"));
        saleRepo.save(new Sale("S3", "INV-103", m3.getId(), m3.getName(), 14, m3.getPurchasePrice(), m3.getSellingPrice(),
                java.time.LocalDateTime.now().minusDays(1), "CASH", null, "Karan"));
    }
}
