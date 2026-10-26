package com.pharmacy.service;

import com.pharmacy.dto.CartItemRequest;
import com.pharmacy.dto.CheckoutRequest;
import com.pharmacy.dto.CreditActionRequest;
import com.pharmacy.dto.InvoiceReceipt;
import com.pharmacy.model.Borrower;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.Sale;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {
    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;
    private final BorrowerService borrowerService;

    public BillingService(MedicineRepository medicineRepository, SaleRepository saleRepository, BorrowerService borrowerService) {
        this.medicineRepository = medicineRepository;
        this.saleRepository = saleRepository;
        this.borrowerService = borrowerService;
    }

    public synchronized InvoiceReceipt checkout(CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout");
        }

        String paymentMode = request.getPaymentMode() != null ? request.getPaymentMode().toUpperCase() : "CASH";
        String borrowerId = request.getBorrowerId();
        String customerName = request.getCustomerName();

        Borrower borrower = null;
        if ("CREDIT".equals(paymentMode)) {
            if (borrowerId == null || borrowerId.trim().isEmpty()) {
                throw new IllegalArgumentException("Borrower must be selected for Credit (Khata) transactions");
            }
            borrower = borrowerService.getBorrower(borrowerId)
                    .orElseThrow(() -> new IllegalArgumentException("Borrower not found with ID: " + borrowerId));
            if (customerName == null || customerName.trim().isEmpty()) {
                customerName = borrower.getCustomerName();
            }
        } else if (customerName == null || customerName.trim().isEmpty()) {
            customerName = "Walk-in Customer";
        }

        // Validate stock for all items first
        for (CartItemRequest item : request.getItems()) {
            Medicine med = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicine not found: " + item.getMedicineId()));
            if (med.getQuantityInStock() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for '" + med.getName() + "'. Available: "
                        + med.getQuantityInStock() + ", Requested: " + item.getQuantity());
            }
        }

        String invoiceNumber = "INV-" + (System.currentTimeMillis() % 10000000);
        LocalDateTime now = LocalDateTime.now();
        List<Sale> completedSales = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;
        BigDecimal grandProfit = BigDecimal.ZERO;

        // Process stock decrement and create Sale records
        for (CartItemRequest item : request.getItems()) {
            Medicine med = medicineRepository.findById(item.getMedicineId()).get();
            med.setQuantityInStock(med.getQuantityInStock() - item.getQuantity());
            medicineRepository.save(med);

            Sale sale = new Sale(
                    "SALE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    invoiceNumber,
                    med.getId(),
                    med.getName(),
                    item.getQuantity(),
                    med.getPurchasePrice(),
                    med.getSellingPrice(),
                    now,
                    paymentMode,
                    borrowerId,
                    customerName
            );

            Sale savedSale = saleRepository.save(sale);
            completedSales.add(savedSale);

            grandTotal = grandTotal.add(savedSale.getTotalRevenue());
            grandProfit = grandProfit.add(savedSale.getGrossProfit());
        }

        BigDecimal borrowerNewBalance = null;
        if ("CREDIT".equals(paymentMode) && borrower != null) {
            CreditActionRequest creditRequest = new CreditActionRequest(
                    grandTotal,
                    "Medicines issued on credit (Invoice: " + invoiceNumber + ")",
                    "CREDIT"
            );
            borrowerService.issueCredit(borrowerId, creditRequest);
            borrowerNewBalance = borrowerService.getBorrower(borrowerId).map(Borrower::getOutstandingBalance).orElse(null);
        }

        return new InvoiceReceipt(
                invoiceNumber,
                now,
                paymentMode,
                customerName,
                borrowerId,
                completedSales,
                grandTotal,
                grandProfit,
                borrowerNewBalance
        );
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public List<Sale> getRecentSales(int limit) {
        return saleRepository.findRecent(limit);
    }
}
