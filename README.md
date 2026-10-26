# 💊 PureMed - Pure Java Pharmacy Management System

A lightweight, clean, and self-contained **Pharmacy Management System** built with **Pure Java 21 / 25 LTS** and **Spring Boot 3.3.x (Web)**. It runs completely out of the box with **zero external database setup** (no MySQL, PostgreSQL, or Docker required).

---

## 🌟 Key Features

### 1. 🛡️ In-Memory Thread-Safe Persistence
- Built entirely on high-performance Java collections: `ConcurrentHashMap<String, T>`, `CopyOnWriteArrayList<T>`, and `AtomicReference`.
- Zero DB installation, zero JPA overhead, zero latency.
- Thread-safe updates for stock counters, financial balances, and billing transactions.

### 2. 💊 Medicine Inventory & Stock Alerts
- **Batch Purchasing & Restocking:** Add daily stock, track batch numbers, unit purchase costs, and retail prices.
- **Stock & Expiry Alert Flags:** Real-time visual alerts for:
  - ⚠️ **Low Stock Items:** Inventory `< 10` units.
  - ⏳ **Expiring Soon Items:** Expiry within 60 days.
  - ❌ **Expired Medicines:** Past expiration date.
- **Inventory Asset Valuation:** Immediate calculation of store inventory value at cost and potential retail revenue.

### 3. 📒 Customer Khata & Credit Ledger
- **Borrower Profiles:** Maintain customer contact details and live outstanding balances.
- **Credit Dispensing:** Issue medicines on credit directly from POS; automatically updates customer balance and logs invoice references.
- **Repayments:** Record partial or full repayments via Cash, UPI, or Card with auto-calculated running ledger balances.
- **Total Store Debt:** Instant tracking of total pending uncollected credit.

### 4. 🛒 Point of Sale (POS) & Counter Dispensing
- Fast medicine search and stock validation.
- Interactive cart builder with quantity controls.
- Supports **Cash**, **UPI**, **Card**, and **Khata Credit**.
- Generates instant printable invoice receipts (`INV-XXXXXX`).

### 5. 📈 Profit & Loss (P&L) Calculator
- Accurately tracks:
  $$\text{Revenue} = \sum (\text{Quantity Sold} \times \text{Selling Price})$$
  $$\text{Cost of Goods Sold (COGS)} = \sum (\text{Quantity Sold} \times \text{Purchase Price})$$
  $$\text{Gross Profit} = \text{Revenue} - \text{COGS}$$
  $$\text{Gross Margin \%} = \left(\frac{\text{Gross Profit}}{\text{Revenue}}\right) \times 100$$
  $$\text{Pending Receivables} = \sum \text{Customer Khata Balances}$$

### 6. 🔮 Deterministic Next-Month Restock Estimator
Pure Java deterministic heuristic algorithm:
1. **Average Daily Run Rate:**
   $$\text{Avg Daily Sales} = \frac{\text{Units Sold in Last } N \text{ Days}}{N}$$
2. **Projected 30-Day Demand:**
   $$\text{Projected Demand} = \text{Avg Daily Sales} \times 30$$
3. **Safety Buffer (20%):**
   $$\text{Buffer} = \text{Projected Demand} \times 0.20$$
4. **Recommended Purchase:**
   $$\text{Recommended Qty} = \max(0, \lceil\text{Projected Demand} + \text{Buffer} - \text{Current Stock}\rceil)$$
5. **Reorder Capital Needed:**
   $$\text{Capital} = \text{Recommended Qty} \times \text{Unit Purchase Price}$$
- Includes a 1-click **"Order & Restock"** shortcut.

---

## 🚀 Quick Start & How to Run

### Option A: Modern Web Application (Recommended)

1. Open a terminal in this directory:
   ```bash
   mvn spring-boot:run
   ```
   *(or use `.\mvnw.cmd spring-boot:run` / double-click `run.bat` on Windows)*

2. Open your browser and navigate to:
   ```
   http://localhost:8080/
   ```

### Option B: Standalone Terminal / CLI Runner

For terminal-only environments:
```bash
# Windows
cli.bat

# Linux / macOS
chmod +x cli.sh
./cli.sh

# Or directly with java:
java -cp target/classes com.pharmacy.cli.StandaloneConsoleApp
```

### Option C: Run Unit Tests
```bash
mvn test
```

---

## 📦 Startup Sample Data (`data/DataLoader.java`)

The system starts pre-populated with realistic pharmacy data for immediate testing:
- **Seller/Admin:** `username: admin`, `password: admin123`
- **9 Sample Medicines:** Paracetamol 500mg, Amoxicillin 500mg, Metformin 500mg, Atorvastatin 20mg, Cetirizine 10mg (Low Stock `< 10`), Azithromycin 500mg (Low Stock), Cough Syrup DX (Expiring in 22 days), Pantoprazole 40mg, Ibuprofen 400mg (Out of Stock).
- **2 Borrowers with Ledger History:**
  - *Rajesh Kumar* (Outstanding Debt: $45.00, partial UPI repayment history)
  - *Priya Sharma* (Outstanding Debt: $24.00)
- **6 Realistic Sales:** Recorded across Cash, UPI, Card, and Credit modes.

---

## 📡 REST API Reference

| Endpoint | Method | Description |
|---|---|---|
| `/api/medicines` | `GET` | List all medicines (supports `?query=` & `?filter=low-stock\|near-expiry\|expired`) |
| `/api/medicines/{id}` | `GET` | Get medicine details by ID |
| `/api/medicines` | `POST` | Create a new medicine SKU |
| `/api/medicines/{id}/restock` | `POST` | Restock batch with new quantity and optional prices |
| `/api/medicines/{id}` | `DELETE` | Delete medicine from inventory |
| `/api/billing/checkout` | `POST` | Process sale (Cash/UPI/Credit), updates stock, logs ledger |
| `/api/billing/sales` | `GET` | Retrieve sales history |
| `/api/borrowers` | `GET` | List all customer borrowers and balances |
| `/api/borrowers` | `POST` | Register a new borrower |
| `/api/borrowers/{id}/credit` | `POST` | Issue credit (increases outstanding debt) |
| `/api/borrowers/{id}/payment` | `POST` | Record repayment (Cash/UPI; decreases debt) |
| `/api/borrowers/{id}/ledger` | `GET` | View audit trail and chronological transaction history |
| `/api/borrowers/total-debt` | `GET` | Get total store pending debt |
| `/api/analytics/pnl` | `GET` | Get P&L report (Revenue, COGS, Profit, Margin %, Debt) |
| `/api/analytics/restock-estimates` | `GET` | Run deterministic restock heuristic (`?days=30`) |
| `/api/auth/login` | `POST` | Admin authentication |
| `/api/auth/me` | `GET` | Get active pharmacy profile |

---

## 📂 Project Structure

```
pharmacy-management-system/
├── pom.xml
├── mvnw.cmd
├── run.bat
├── cli.bat
├── cli.sh
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/
│   │   │   ├── PharmacyApplication.java
│   │   │   ├── model/
│   │   │   │   ├── Seller.java
│   │   │   │   ├── Medicine.java
│   │   │   │   ├── Borrower.java
│   │   │   │   ├── CreditTransaction.java
│   │   │   │   ├── Sale.java
│   │   │   │   ├── RestockRecommendation.java
│   │   │   │   └── ProfitLossSummary.java
│   │   │   ├── repository/
│   │   │   │   ├── SellerRepository.java
│   │   │   │   ├── MedicineRepository.java
│   │   │   │   ├── BorrowerRepository.java
│   │   │   │   ├── CreditTransactionRepository.java
│   │   │   │   └── SaleRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── BorrowerService.java
│   │   │   │   ├── BillingService.java
│   │   │   │   └── AnalyticsService.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── MedicineController.java
│   │   │   │   ├── BillingController.java
│   │   │   │   ├── BorrowerController.java
│   │   │   │   └── AnalyticsController.java
│   │   │   ├── cli/
│   │   │   │   └── StandaloneConsoleApp.java
│   │   │   └── data/
│   │   │       └── DataLoader.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/styles.css
│   │           └── js/app.js
│   └── test/java/com/pharmacy/
│       └── PharmacyApplicationTests.java
```
