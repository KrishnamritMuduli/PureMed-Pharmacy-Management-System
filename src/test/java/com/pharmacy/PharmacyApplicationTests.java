package com.pharmacy;

import com.pharmacy.dto.BatchRestockRequest;
import com.pharmacy.dto.CartItemRequest;
import com.pharmacy.dto.CheckoutRequest;
import com.pharmacy.dto.CreditActionRequest;
import com.pharmacy.dto.InvoiceReceipt;
import com.pharmacy.model.*;
import com.pharmacy.repository.*;
import com.pharmacy.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PharmacyApplicationTests {

    private MedicineRepository medicineRepository;
    private BorrowerRepository borrowerRepository;
    private CreditTransactionRepository creditTxRepository;
    private SaleRepository saleRepository;
    private InventoryService inventoryService;
    private BorrowerService borrowerService;
    private BillingService billingService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        medicineRepository = new MedicineRepository();
        borrowerRepository = new BorrowerRepository();
        creditTxRepository = new CreditTransactionRepository();
        saleRepository = new SaleRepository();

        inventoryService = new InventoryService(medicineRepository);
        borrowerService = new BorrowerService(borrowerRepository, creditTxRepository);
        billingService = new BillingService(medicineRepository, saleRepository, borrowerService);
        analyticsService = new AnalyticsService(saleRepository, medicineRepository, borrowerService);
    }

    @Test
    void testInventoryAndBatchRestock() {
        Medicine med = new Medicine(
                "TEST-MED-1", "Test Aspirin", "Acetylsalicylic Acid", "BAT-001",
                15, new BigDecimal("2.00"), new BigDecimal("5.00"),
                LocalDate.now().plusMonths(6), "Painkiller", "Test Pharma"
        );
        inventoryService.addMedicine(med);

        assertFalse(med.isLowStock());

        // Restock
        BatchRestockRequest restock = new BatchRestockRequest(10, "BAT-002", null, null, null);
        Medicine updated = inventoryService.restockBatch("TEST-MED-1", restock);
        assertEquals(25, updated.getQuantityInStock());
        assertEquals("BAT-002", updated.getBatchNumber());
    }

    @Test
    void testBorrowerCreditAndRepayment() {
        Borrower b = new Borrower("BOR-T1", "John Doe", "1234567890", "john@test.com", "Main St", BigDecimal.ZERO);
        borrowerService.createBorrower(b);

        assertEquals(BigDecimal.ZERO, b.getOutstandingBalance());

        // Issue credit
        CreditActionRequest creditReq = new CreditActionRequest(new BigDecimal("50.00"), "Emergency medicine", "CREDIT");
        CreditTransaction tx1 = borrowerService.issueCredit(b.getId(), creditReq);
        assertEquals(new BigDecimal("50.00"), tx1.getRunningBalance());
        assertEquals(new BigDecimal("50.00"), borrowerService.getBorrower(b.getId()).get().getOutstandingBalance());

        // Repay part
        CreditActionRequest payReq = new CreditActionRequest(new BigDecimal("20.00"), "Cash payment", "CASH");
        CreditTransaction tx2 = borrowerService.recordPayment(b.getId(), payReq);
        assertEquals(new BigDecimal("30.00"), tx2.getRunningBalance());
        assertEquals(new BigDecimal("30.00"), borrowerService.getBorrower(b.getId()).get().getOutstandingBalance());

        List<CreditTransaction> ledger = borrowerService.getLedger(b.getId());
        assertEquals(2, ledger.size());
    }

    @Test
    void testBillingAndInventoryDecrement() {
        Medicine med = new Medicine(
                "MED-BILL", "Amoxil", "Amoxicillin", "B1",
                20, new BigDecimal("3.00"), new BigDecimal("7.00"),
                LocalDate.now().plusMonths(12), "Antibiotic", "Sun"
        );
        inventoryService.addMedicine(med);

        CheckoutRequest req = new CheckoutRequest(
                List.of(new CartItemRequest(med.getId(), 5)),
                "CASH", null, "Jane"
        );

        InvoiceReceipt receipt = billingService.checkout(req);
        assertNotNull(receipt.getInvoiceNumber());
        assertEquals(new BigDecimal("35.00"), receipt.getGrandTotal()); // 5 * 7.00
        assertEquals(new BigDecimal("20.00"), receipt.getTotalGrossProfit()); // 5 * (7.00 - 3.00)

        // Check stock reduced
        Medicine updatedMed = inventoryService.getMedicine(med.getId()).get();
        assertEquals(15, updatedMed.getQuantityInStock());
    }

    @Test
    void testProfitLossAndRestockEstimator() {
        Medicine med = new Medicine(
                "MED-STAT", "PainAway", "Paracetamol", "P1",
                5, new BigDecimal("1.00"), new BigDecimal("4.00"), // Stock is 5
                LocalDate.now().plusMonths(6), "Analgesic", "Cipla"
        );
        inventoryService.addMedicine(med);

        // Record a sale of 30 units over last 30 days
        Sale sale = new Sale(
                "S-1", "INV-1", med.getId(), med.getName(),
                30, med.getPurchasePrice(), med.getSellingPrice(),
                java.time.LocalDateTime.now().minusDays(5), "CASH", null, "Customer"
        );
        saleRepository.save(sale);

        // Check P&L
        ProfitLossSummary pnl = analyticsService.calculateProfitLoss(30);
        assertEquals(new BigDecimal("120.00"), pnl.getTotalRevenue()); // 30 * 4
        assertEquals(new BigDecimal("30.00"), pnl.getTotalCogs()); // 30 * 1
        assertEquals(new BigDecimal("90.00"), pnl.getGrossProfit()); // 120 - 30
        assertEquals(75.0, pnl.getGrossMarginPercentage());

        // Check Restock heuristic:
        // Avg daily sales = 30 sold / 30 days = 1.0/day
        // Projected 30-Day Demand = 1.0 * 30 = 30
        // Safety Buffer 20% = 30 * 0.20 = 6
        // Current Stock = 5
        // Recommended = max(0, ceil(30 + 6 - 5)) = 31
        List<RestockRecommendation> recs = analyticsService.calculateRestockEstimates(30);
        RestockRecommendation r = recs.stream().filter(x -> x.getMedicineId().equals(med.getId())).findFirst().get();

        assertEquals(1.0, r.getAvgDailySales(), 0.01);
        assertEquals(30.0, r.getProjected30DayDemand(), 0.01);
        assertEquals(6.0, r.getBufferStock(), 0.01);
        assertEquals(31, r.getRecommendedPurchaseQty());
        assertEquals(new BigDecimal("31.00"), r.getEstimatedReorderCost()); // 31 * 1.00
    }
}
