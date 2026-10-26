package com.pharmacy.data;

import com.pharmacy.dto.RegisterSellerRequest;
import com.pharmacy.model.*;
import com.pharmacy.repository.*;
import com.pharmacy.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {
    private final AuthService authService;
    private final MedicineRepository medicineRepository;
    private final BorrowerRepository borrowerRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final SaleRepository saleRepository;

    public DataLoader(AuthService authService,
                      MedicineRepository medicineRepository,
                      BorrowerRepository borrowerRepository,
                      CreditTransactionRepository creditTransactionRepository,
                      SaleRepository saleRepository) {
        this.authService = authService;
        this.medicineRepository = medicineRepository;
        this.borrowerRepository = borrowerRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.saleRepository = saleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Pre-populate Seller/Admin Profile
        RegisterSellerRequest sellerReq = new RegisterSellerRequest(
                "Apex Medico & Wellness Pharmacy",
                "admin",
                "admin123",
                "+91 98765 43210",
                "apex.pharmacy@healthcare.org",
                "42 Medical Square, Central Avenue, Tech City"
        );
        authService.register(sellerReq);

        // 2. Pre-populate Medicines (with normal, low stock, near-expiry, and stockout)
        Medicine med1 = new Medicine(
                "MED-PCM500", "Paracetamol 500mg", "Acetaminophen", "BAT-2026-A1",
                120, new BigDecimal("1.50"), new BigDecimal("3.00"),
                LocalDate.now().plusMonths(18), "Analgesic", "Cipla Ltd"
        );
        Medicine med2 = new Medicine(
                "MED-AMX500", "Amoxicillin 500mg", "Amoxicillin Trihydrate", "BAT-2026-B4",
                45, new BigDecimal("4.20"), new BigDecimal("8.50"),
                LocalDate.now().plusMonths(12), "Antibiotic", "Sun Pharma"
        );
        Medicine med3 = new Medicine(
                "MED-MET500", "Metformin 500mg", "Metformin Hydrochloride", "BAT-2026-C2",
                80, new BigDecimal("2.10"), new BigDecimal("5.00"),
                LocalDate.now().plusMonths(14), "Antidiabetic", "Dr. Reddy's"
        );
        Medicine med4 = new Medicine(
                "MED-ATR020", "Atorvastatin 20mg", "Atorvastatin Calcium", "BAT-2026-D1",
                60, new BigDecimal("6.00"), new BigDecimal("12.00"),
                LocalDate.now().plusMonths(10), "Cardiovascular", "Lupin Ltd"
        );
        // Low Stock (< 10)
        Medicine med5 = new Medicine(
                "MED-CTZ010", "Cetirizine 10mg", "Cetirizine HCl", "BAT-2026-E5",
                8, new BigDecimal("0.80"), new BigDecimal("2.50"),
                LocalDate.now().plusMonths(8), "Antihistamine", "Torrent Pharma"
        );
        // Low Stock (< 10)
        Medicine med6 = new Medicine(
                "MED-AZI500", "Azithromycin 500mg", "Azithromycin Dihydrate", "BAT-2026-F9",
                5, new BigDecimal("8.00"), new BigDecimal("16.00"),
                LocalDate.now().plusMonths(15), "Antibiotic", "Abbott Healthcare"
        );
        // Near Expiry (< 30 days)
        Medicine med7 = new Medicine(
                "MED-CGH100", "Cough Relief Syrup DX", "Dextromethorphan + CPM", "BAT-2025-X3",
                25, new BigDecimal("3.50"), new BigDecimal("7.00"),
                LocalDate.now().plusDays(22), "Respiratory", "Glenmark"
        );
        Medicine med8 = new Medicine(
                "MED-PAN040", "Pantoprazole 40mg", "Pantoprazole Sodium", "BAT-2026-G7",
                95, new BigDecimal("2.00"), new BigDecimal("4.50"),
                LocalDate.now().plusMonths(20), "Gastrointestinal", "Alkem Labs"
        );
        // Out of Stock (0 quantity)
        Medicine med9 = new Medicine(
                "MED-IBU400", "Ibuprofen 400mg", "Ibuprofen BP", "BAT-2026-H1",
                0, new BigDecimal("1.80"), new BigDecimal("4.00"),
                LocalDate.now().plusMonths(9), "Anti-inflammatory", "Zydus Cadila"
        );

        medicineRepository.save(med1);
        medicineRepository.save(med2);
        medicineRepository.save(med3);
        medicineRepository.save(med4);
        medicineRepository.save(med5);
        medicineRepository.save(med6);
        medicineRepository.save(med7);
        medicineRepository.save(med8);
        medicineRepository.save(med9);

        // 3. Pre-populate 2 Borrowers with Ledger records
        Borrower b1 = new Borrower(
                "BOR-1001", "Rajesh Kumar", "+91 98111 22233",
                "rajesh.k@gmail.com", "House #12, Market Street, Block B",
                new BigDecimal("45.00")
        );
        borrowerRepository.save(b1);

        CreditTransaction tx1 = new CreditTransaction(
                "TX-101", b1.getId(), new BigDecimal("60.00"),
                CreditTransaction.Type.CREDIT_TAKEN,
                LocalDateTime.now().minusDays(10),
                "Purchase of monthly diabetes & blood pressure medicines",
                "CREDIT", new BigDecimal("60.00")
        );
        CreditTransaction tx2 = new CreditTransaction(
                "TX-102", b1.getId(), new BigDecimal("15.00"),
                CreditTransaction.Type.PAYMENT_MADE,
                LocalDateTime.now().minusDays(3),
                "Part payment received via UPI",
                "UPI", new BigDecimal("45.00")
        );
        creditTransactionRepository.save(tx1);
        creditTransactionRepository.save(tx2);

        Borrower b2 = new Borrower(
                "BOR-1002", "Priya Sharma", "+91 98444 55566",
                "priya.s@yahoo.com", "Apartment 4B, Sunrise Heights",
                new BigDecimal("24.00")
        );
        borrowerRepository.save(b2);

        CreditTransaction tx3 = new CreditTransaction(
                "TX-103", b2.getId(), new BigDecimal("24.00"),
                CreditTransaction.Type.CREDIT_TAKEN,
                LocalDateTime.now().minusDays(5),
                "Antibiotics and syrup on credit",
                "CREDIT", new BigDecimal("24.00")
        );
        creditTransactionRepository.save(tx3);

        // 4. Pre-populate 5+ Sales records across various days
        Sale s1 = new Sale(
                "SALE-201", "INV-10001", med1.getId(), med1.getName(),
                15, med1.getPurchasePrice(), med1.getSellingPrice(),
                LocalDateTime.now().minusDays(6), "CASH", null, "Walk-in Patient"
        );
        Sale s2 = new Sale(
                "SALE-202", "INV-10002", med2.getId(), med2.getName(),
                6, med2.getPurchasePrice(), med2.getSellingPrice(),
                LocalDateTime.now().minusDays(4), "UPI", null, "Vikas Gupta"
        );
        Sale s3 = new Sale(
                "SALE-203", "INV-10003", med3.getId(), med3.getName(),
                12, med3.getPurchasePrice(), med3.getSellingPrice(),
                LocalDateTime.now().minusDays(3), "CASH", null, "Anjali Mehta"
        );
        Sale s4 = new Sale(
                "SALE-204", "INV-10004", med4.getId(), med4.getName(),
                5, med4.getPurchasePrice(), med4.getSellingPrice(),
                LocalDateTime.now().minusDays(2), "CARD", null, "Suresh Nair"
        );
        Sale s5 = new Sale(
                "SALE-205", "INV-10005", med8.getId(), med8.getName(),
                8, med8.getPurchasePrice(), med8.getSellingPrice(),
                LocalDateTime.now().minusDays(1), "CREDIT", b1.getId(), b1.getCustomerName()
        );
        Sale s6 = new Sale(
                "SALE-206", "INV-10006", med5.getId(), med5.getName(),
                14, med5.getPurchasePrice(), med5.getSellingPrice(),
                LocalDateTime.now().minusHours(5), "UPI", null, "Karan Singh"
        );

        saleRepository.save(s1);
        saleRepository.save(s2);
        saleRepository.save(s3);
        saleRepository.save(s4);
        saleRepository.save(s5);
        saleRepository.save(s6);

        System.out.println("===============================================================");
        System.out.println(" [PHARMACY MANAGEMENT SYSTEM] Data Loaded Successfully!");
        System.out.println(" - 9 Sample Medicines registered (including Low Stock & Expiring)");
        System.out.println(" - 2 Borrowers with active Credit/Ledger history loaded");
        System.out.println(" - 6 Realistic Sales recorded across different payment modes");
        System.out.println(" - Admin credentials: username='admin', password='admin123'");
        System.out.println("===============================================================");
    }
}
