package com.pharmacy.service;

import com.pharmacy.dto.CreditActionRequest;
import com.pharmacy.model.Borrower;
import com.pharmacy.model.CreditTransaction;
import com.pharmacy.repository.BorrowerRepository;
import com.pharmacy.repository.CreditTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public BorrowerService(BorrowerRepository borrowerRepository, CreditTransactionRepository creditTransactionRepository) {
        this.borrowerRepository = borrowerRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    public Borrower createBorrower(Borrower borrower) {
        if (borrower.getCustomerName() == null || borrower.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (borrower.getPhoneNumber() == null || borrower.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer phone number is required");
        }
        if (borrower.getOutstandingBalance() == null) {
            borrower.setOutstandingBalance(BigDecimal.ZERO);
        }
        Borrower saved = borrowerRepository.save(borrower);

        if (saved.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            CreditTransaction initialTx = new CreditTransaction(
                    null,
                    saved.getId(),
                    saved.getOutstandingBalance(),
                    CreditTransaction.Type.CREDIT_TAKEN,
                    LocalDateTime.now(),
                    "Opening Credit Balance",
                    "OPENING_BALANCE",
                    saved.getOutstandingBalance()
            );
            creditTransactionRepository.save(initialTx);
        }

        return saved;
    }

    public Optional<Borrower> getBorrower(String id) {
        return borrowerRepository.findById(id);
    }

    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAll();
    }

    public List<Borrower> searchBorrowers(String query) {
        return borrowerRepository.search(query);
    }

    public CreditTransaction issueCredit(String borrowerId, CreditActionRequest request) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + borrowerId));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be greater than zero");
        }

        synchronized (borrower) {
            borrower.addDebt(request.getAmount());
            borrowerRepository.save(borrower);

            String note = request.getNote() != null && !request.getNote().trim().isEmpty()
                    ? request.getNote().trim()
                    : "Credit taken for pharmacy purchase";

            CreditTransaction tx = new CreditTransaction(
                    null,
                    borrowerId,
                    request.getAmount(),
                    CreditTransaction.Type.CREDIT_TAKEN,
                    LocalDateTime.now(),
                    note,
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "CREDIT",
                    borrower.getOutstandingBalance()
            );
            return creditTransactionRepository.save(tx);
        }
    }

    public CreditTransaction recordPayment(String borrowerId, CreditActionRequest request) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + borrowerId));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }

        synchronized (borrower) {
            borrower.reduceDebt(request.getAmount());
            borrowerRepository.save(borrower);

            String note = request.getNote() != null && !request.getNote().trim().isEmpty()
                    ? request.getNote().trim()
                    : "Debt repayment received";

            CreditTransaction tx = new CreditTransaction(
                    null,
                    borrowerId,
                    request.getAmount(),
                    CreditTransaction.Type.PAYMENT_MADE,
                    LocalDateTime.now(),
                    note,
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH",
                    borrower.getOutstandingBalance()
            );
            return creditTransactionRepository.save(tx);
        }
    }

    public List<CreditTransaction> getLedger(String borrowerId) {
        return creditTransactionRepository.findByBorrowerId(borrowerId);
    }

    public BigDecimal getTotalOutstandingDebt() {
        return borrowerRepository.calculateTotalOutstandingDebt();
    }
}
