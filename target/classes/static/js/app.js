// Global State
let allMedicines = [];
let allBorrowers = [];
let cart = []; // { medicine, quantity }
let currentFilter = 'all';
let activeBorrowerForLedger = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    loadSellerProfile();
    loadDashboard();
    loadMedicinesData();
    loadBorrowersData();
});

// Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${type === 'error' ? '❌' : (type === 'success' ? '✅' : 'ℹ️')}</span> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Format Currency
function formatCurrency(val) {
    if (val === null || val === undefined) return '$0.00';
    return '$' + Number(val).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// Modal Helpers
function openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.style.display = 'flex';
}

function closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.style.display = 'none';
}

// Tab Switching
function switchTab(tabName) {
    document.querySelectorAll('.nav-tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));

    const btn = Array.from(document.querySelectorAll('.nav-tab-btn')).find(b => b.getAttribute('onclick')?.includes(tabName));
    if (btn) btn.classList.add('active');

    const pane = document.getElementById(`tab-${tabName}`);
    if (pane) pane.classList.add('active');

    // Tab-specific refreshes
    if (tabName === 'dashboard') loadDashboard();
    if (tabName === 'pos') refreshPosCatalog();
    if (tabName === 'inventory') loadMedicinesData();
    if (tabName === 'khata') loadKhataData();
    if (tabName === 'pnl') loadPnlData();
    if (tabName === 'restock') loadRestockData();
}

// ----------------- 1. SELLER & AUTH -----------------
async function loadSellerProfile() {
    try {
        const res = await fetch('/api/auth/me');
        if (res.ok) {
            const seller = await res.json();
            document.getElementById('pharmacyNameHeader').textContent = seller.pharmacyName || 'PureMed Pharmacy';
            document.getElementById('sellerUserBadge').textContent = `👤 ${seller.username || 'Admin'}`;
            document.getElementById('sellerProfilePharmacyName').textContent = seller.pharmacyName || 'Pharmacy';
            document.getElementById('sellerProfileUsername').textContent = seller.username || '-';
            document.getElementById('sellerProfilePhone').textContent = seller.contactPhone || '-';
            document.getElementById('sellerProfileEmail').textContent = seller.email || '-';
            document.getElementById('sellerProfileAddress').textContent = seller.address || '-';
        }
    } catch (e) {
        console.error('Failed to load seller profile', e);
    }
}

function openSellerModal() {
    openModal('modalSeller');
}

// ----------------- 2. DASHBOARD -----------------
async function loadDashboard() {
    try {
        const pnlRes = await fetch('/api/analytics/pnl');
        if (pnlRes.ok) {
            const pnl = await pnlRes.json();
            document.getElementById('kpiRevenue').textContent = formatCurrency(pnl.totalRevenue);
            document.getElementById('kpiTransactions').textContent = `${pnl.totalTransactions} transactions (${pnl.totalUnitsSold} units)`;
            document.getElementById('kpiGrossProfit').textContent = formatCurrency(pnl.grossProfit);
            document.getElementById('kpiMargin').textContent = `Gross Margin: ${pnl.grossMarginPercentage}%`;
            document.getElementById('kpiDebt').textContent = formatCurrency(pnl.pendingReceivables);
            document.getElementById('kpiInventoryCost').textContent = formatCurrency(pnl.inventoryValueAtCost);
            document.getElementById('kpiInventoryRetail').textContent = `Retail Potential: ${formatCurrency(pnl.inventoryValueAtRetail)}`;
            document.getElementById('kpiLowStock').textContent = pnl.lowStockCount;
            document.getElementById('kpiNearExpiry').textContent = pnl.nearExpiryCount;
            document.getElementById('kpiExpiredText').textContent = `${pnl.expiredCount} expired`;
        }

        const recentRes = await fetch('/api/billing/recent?limit=10');
        if (recentRes.ok) {
            const sales = await recentRes.json();
            renderRecentSalesTable(sales);
        }
    } catch (e) {
        console.error('Failed to load dashboard data', e);
    }
}

function renderRecentSalesTable(sales) {
    const tbody = document.getElementById('recentSalesTableBody');
    if (!sales || sales.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No sales recorded yet. Dispense medicine to create sales.</td></tr>';
        return;
    }

    tbody.innerHTML = sales.map(s => {
        const dateStr = new Date(s.saleDate).toLocaleString();
        let badgeClass = 'badge-info';
        if (s.paymentMode === 'CASH') badgeClass = 'badge-success';
        if (s.paymentMode === 'CREDIT') badgeClass = 'badge-danger';
        if (s.paymentMode === 'UPI') badgeClass = 'badge-info';

        return `
            <tr>
                <td><strong>${s.invoiceNumber || s.id}</strong></td>
                <td><strong>${s.medicineName}</strong></td>
                <td>${s.quantitySold}</td>
                <td>${formatCurrency(s.sellingPricePerUnit)}</td>
                <td><strong>${formatCurrency(s.totalRevenue)}</strong></td>
                <td style="color:var(--success); font-weight:600;">${formatCurrency(s.grossProfit)}</td>
                <td><span class="badge ${badgeClass}">${s.paymentMode}</span></td>
                <td>${s.customerName || 'Walk-in'}</td>
                <td style="font-size:0.8rem; color:var(--text-secondary);">${dateStr}</td>
            </tr>
        `;
    }).join('');
}

// ----------------- 3. POINT OF SALE (POS) -----------------
async function refreshPosCatalog() {
    await loadMedicinesData();
    await loadBorrowersData();

    const select = document.getElementById('posMedicineSelect');
    select.innerHTML = '<option value="">-- Choose Medicine from Inventory --</option>' +
        allMedicines.map(m => {
            const status = m.quantityInStock <= 0 ? ' (OUT OF STOCK)' : (m.quantityInStock < 10 ? ' (LOW STOCK)' : '');
            return `<option value="${m.id}">${m.name} | Stock: ${m.quantityInStock}${status} | $${m.sellingPrice}</option>`;
        }).join('');

    const borrowerSelect = document.getElementById('posBorrowerSelect');
    borrowerSelect.innerHTML = '<option value="">-- Choose Existing Borrower --</option>' +
        allBorrowers.map(b => `<option value="${b.id}">${b.customerName} (Phone: ${b.phoneNumber} | Debt: ${formatCurrency(b.outstandingBalance)})</option>`).join('');

    renderCart();
}

function onPosMedicineSelect() {
    const medId = document.getElementById('posMedicineSelect').value;
    const detailsDiv = document.getElementById('posMedicineDetails');

    if (!medId) {
        detailsDiv.style.display = 'none';
        return;
    }

    const med = allMedicines.find(m => m.id === medId);
    if (!med) return;

    detailsDiv.style.display = 'block';
    document.getElementById('posMedName').textContent = med.name;
    document.getElementById('posMedGeneric').textContent = `Generic: ${med.genericName || 'N/A'} | Batch: ${med.batchNumber || 'N/A'}`;
    document.getElementById('posMedPrice').textContent = Number(med.sellingPrice).toFixed(2);

    const badge = document.getElementById('posMedStockBadge');
    badge.textContent = `${med.quantityInStock} in stock`;
    if (med.quantityInStock <= 0) {
        badge.className = 'badge badge-danger';
        badge.textContent = 'Out of Stock';
    } else if (med.quantityInStock < 10) {
        badge.className = 'badge badge-warning';
    } else {
        badge.className = 'badge badge-success';
    }

    const qtyInput = document.getElementById('posQtyInput');
    qtyInput.value = 1;
    qtyInput.max = med.quantityInStock;
}

function addToCart() {
    const medId = document.getElementById('posMedicineSelect').value;
    if (!medId) {
        showToast('Please select a medicine first', 'error');
        return;
    }

    const med = allMedicines.find(m => m.id === medId);
    if (!med) return;

    const qty = parseInt(document.getElementById('posQtyInput').value) || 1;
    if (qty <= 0) {
        showToast('Quantity must be at least 1', 'error');
        return;
    }

    // Check cart existing qty
    const existingIndex = cart.findIndex(c => c.medicine.id === med.id);
    const alreadyInCart = existingIndex >= 0 ? cart[existingIndex].quantity : 0;
    const requestedTotal = alreadyInCart + qty;

    if (requestedTotal > med.quantityInStock) {
        showToast(`Cannot add ${qty}. Only ${med.quantityInStock - alreadyInCart} more available in stock!`, 'error');
        return;
    }

    if (existingIndex >= 0) {
        cart[existingIndex].quantity += qty;
    } else {
        cart.push({ medicine: med, quantity: qty });
    }

    showToast(`Added ${qty}x ${med.name} to bill`, 'success');
    renderCart();
}

function updateCartQty(index, change) {
    if (!cart[index]) return;
    const newQty = cart[index].quantity + change;
    if (newQty <= 0) {
        cart.splice(index, 1);
    } else {
        if (newQty > cart[index].medicine.quantityInStock) {
            showToast(`Max available stock is ${cart[index].medicine.quantityInStock}`, 'error');
            return;
        }
        cart[index].quantity = newQty;
    }
    renderCart();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    renderCart();
}

function renderCart() {
    const container = document.getElementById('cartItemsContainer');
    if (cart.length === 0) {
        container.innerHTML = '<div style="padding:2rem; text-align:center; color:var(--text-muted);" id="emptyCartNotice">No medicines added yet. Select a medicine above to begin.</div>';
        document.getElementById('billSubtotal').textContent = '$0.00';
        document.getElementById('billProfit').textContent = '$0.00';
        document.getElementById('billGrandTotal').textContent = '$0.00';
        return;
    }

    let subtotal = 0;
    let profit = 0;

    container.innerHTML = cart.map((item, idx) => {
        const itemTotal = item.quantity * item.medicine.sellingPrice;
        const itemProfit = item.quantity * (item.medicine.sellingPrice - item.medicine.purchasePrice);
        subtotal += itemTotal;
        profit += itemProfit;

        return `
            <div class="cart-item-row">
                <div style="flex:1;">
                    <div style="font-weight:700;">${item.medicine.name}</div>
                    <div style="font-size:0.78rem; color:var(--text-secondary);">$${item.medicine.sellingPrice} each</div>
                </div>
                <div class="qty-control">
                    <button class="qty-btn" onclick="updateCartQty(${idx}, -1)">-</button>
                    <span style="font-weight:700; width:30px; text-align:center;">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateCartQty(${idx}, 1)">+</button>
                </div>
                <div style="width:90px; text-align:right; font-weight:700; color:var(--primary);">
                    ${formatCurrency(itemTotal)}
                </div>
                <div style="margin-left:0.75rem;">
                    <button class="btn btn-secondary btn-sm" style="color:var(--danger); padding:0.2rem 0.4rem;" onclick="removeFromCart(${idx})">🗑️</button>
                </div>
            </div>
        `;
    }).join('');

    document.getElementById('billSubtotal').textContent = formatCurrency(subtotal);
    document.getElementById('billProfit').textContent = formatCurrency(profit);
    document.getElementById('billGrandTotal').textContent = formatCurrency(subtotal);
}

function onPaymentModeChange() {
    const mode = document.getElementById('posPaymentMode').value;
    const group = document.getElementById('borrowerSelectGroup');
    if (mode === 'CREDIT') {
        group.style.display = 'block';
    } else {
        group.style.display = 'none';
    }
}

function onBorrowerSelectChange() {
    const borrowerId = document.getElementById('posBorrowerSelect').value;
    const notice = document.getElementById('borrowerDebtNotice');
    if (!borrowerId) {
        notice.textContent = '';
        return;
    }
    const b = allBorrowers.find(x => x.id === borrowerId);
    if (b) {
        document.getElementById('posCustomerName').value = b.customerName;
        notice.textContent = `Current unpaid debt: ${formatCurrency(b.outstandingBalance)}. This purchase will be added to ledger.`;
    }
}

async function processCheckout() {
    if (cart.length === 0) {
        showToast('Cart is empty! Add medicines before checkout', 'error');
        return;
    }

    const paymentMode = document.getElementById('posPaymentMode').value;
    const customerName = document.getElementById('posCustomerName').value.trim();
    const borrowerId = document.getElementById('posBorrowerSelect').value;

    if (paymentMode === 'CREDIT' && !borrowerId) {
        showToast('Please select a borrower account for credit purchases', 'error');
        return;
    }

    const payload = {
        items: cart.map(c => ({ medicineId: c.medicine.id, quantity: c.quantity })),
        paymentMode: paymentMode,
        borrowerId: borrowerId || null,
        customerName: customerName || 'Walk-in Customer'
    };

    try {
        const res = await fetch('/api/billing/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) {
            showToast(data.error || 'Checkout failed', 'error');
            return;
        }

        showToast('Sale completed successfully!', 'success');
        showInvoiceReceipt(data);

        // Reset cart
        cart = [];
        renderCart();
        document.getElementById('posMedicineSelect').value = '';
        document.getElementById('posMedicineDetails').style.display = 'none';
        document.getElementById('posCustomerName').value = '';

        // Reload data
        loadMedicinesData();
        loadBorrowersData();
        loadDashboard();

    } catch (e) {
        showToast('Error during checkout: ' + e.message, 'error');
    }
}

function showInvoiceReceipt(receipt) {
    const body = document.getElementById('invoiceReceiptBody');
    const pharmacyName = document.getElementById('pharmacyNameHeader').textContent;
    const dateFormatted = new Date(receipt.invoiceDate).toLocaleString();

    let itemsHtml = receipt.items.map(it => {
        return `
            <div style="display:flex; justify-content:space-between; margin-bottom:0.25rem;">
                <span style="flex:1;">${it.medicineName} (x${it.quantitySold})</span>
                <span>${formatCurrency(it.totalRevenue)}</span>
            </div>
        `;
    }).join('');

    let creditNotice = '';
    if (receipt.paymentMode === 'CREDIT') {
        creditNotice = `
            <div style="margin-top:0.75rem; padding:0.5rem; background:#fee2e2; border-radius:4px; font-size:0.8rem; color:#991b1b;">
                <strong>KHATA CREDIT NOTE:</strong><br>
                Added to customer ledger.<br>
                New Total Debt: ${formatCurrency(receipt.borrowerNewBalance)}
            </div>
        `;
    }

    body.innerHTML = `
        <div style="text-align:center; border-bottom:1px dashed #000; padding-bottom:0.5rem; margin-bottom:0.75rem;">
            <div style="font-size:1.1rem; font-weight:bold;">${pharmacyName}</div>
            <div style="font-size:0.8rem;">INVOICE: ${receipt.invoiceNumber}</div>
            <div style="font-size:0.75rem; color:#555;">Date: ${dateFormatted}</div>
            <div style="font-size:0.75rem; color:#555;">Customer: ${receipt.customerName} | Mode: ${receipt.paymentMode}</div>
        </div>
        <div style="margin-bottom:0.75rem;">
            ${itemsHtml}
        </div>
        <div style="border-top:1px dashed #000; padding-top:0.5rem; display:flex; justify-content:space-between; font-weight:bold; font-size:1.05rem;">
            <span>GRAND TOTAL:</span>
            <span>${formatCurrency(receipt.grandTotal)}</span>
        </div>
        ${creditNotice}
        <div style="text-align:center; margin-top:1rem; font-size:0.75rem; color:#777;">
            Thank you for choosing ${pharmacyName}!<br>
            Get well soon!
        </div>
    `;

    openModal('modalInvoiceReceipt');
}

// ----------------- 4. MEDICINE INVENTORY -----------------
async function loadMedicinesData() {
    try {
        let url = '/api/medicines';
        if (currentFilter && currentFilter !== 'all') {
            url += `?filter=${currentFilter}`;
        }
        const res = await fetch(url);
        if (res.ok) {
            allMedicines = await res.json();
            renderInventoryTable(allMedicines);
        }
    } catch (e) {
        console.error('Failed to load medicines', e);
    }
}

function setInventoryFilter(filter, btn) {
    currentFilter = filter;
    document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    loadMedicinesData();
}

function filterInventoryTable() {
    const q = document.getElementById('inventorySearch').value.toLowerCase().trim();
    if (!q) {
        renderInventoryTable(allMedicines);
        return;
    }
    const filtered = allMedicines.filter(m =>
        (m.name && m.name.toLowerCase().includes(q)) ||
        (m.genericName && m.genericName.toLowerCase().includes(q)) ||
        (m.batchNumber && m.batchNumber.toLowerCase().includes(q)) ||
        (m.category && m.category.toLowerCase().includes(q))
    );
    renderInventoryTable(filtered);
}

function renderInventoryTable(medicines) {
    const tbody = document.getElementById('inventoryTableBody');
    if (!medicines || medicines.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No medicines found matching criteria.</td></tr>';
        return;
    }

    tbody.innerHTML = medicines.map(m => {
        let stockBadge = 'badge-success';
        if (m.quantityInStock <= 0) stockBadge = 'badge-danger';
        else if (m.quantityInStock < 10) stockBadge = 'badge-warning';

        let alertBadge = '<span class="badge badge-success">Healthy Stock</span>';
        if (m.quantityInStock <= 0) {
            alertBadge = '<span class="badge badge-danger">Out of Stock!</span>';
        } else if (m.quantityInStock < 10) {
            alertBadge = '<span class="badge badge-warning">Low Stock (&lt;10)</span>';
        }

        const expiryDate = m.expiryDate || 'N/A';
        const isExp = m.expired;
        const isNear = m.nearExpiry;

        let expiryStatus = '';
        if (isExp) {
            expiryStatus = '<br><span class="badge badge-danger">EXPIRED</span>';
        } else if (isNear) {
            expiryStatus = '<br><span class="badge badge-warning">Expiring Soon</span>';
        }

        const margin = Number(m.sellingPrice) - Number(m.purchasePrice);

        return `
            <tr>
                <td>
                    <strong>${m.name}</strong>
                    <div style="font-size:0.75rem; color:var(--text-secondary);">${m.genericName || '-'} | ${m.category || 'General'}</div>
                </td>
                <td><code>${m.batchNumber || '-'}</code></td>
                <td><span class="badge ${stockBadge}" style="font-size:0.85rem;">${m.quantityInStock} units</span></td>
                <td>${formatCurrency(m.purchasePrice)}</td>
                <td><strong>${formatCurrency(m.sellingPrice)}</strong></td>
                <td style="color:var(--success); font-weight:600;">+${formatCurrency(margin)}</td>
                <td>${expiryDate}${expiryStatus}</td>
                <td>${alertBadge}</td>
                <td>
                    <div style="display:flex; gap:0.35rem;">
                        <button class="btn btn-secondary btn-sm" onclick="openRestockBatchModal('${m.id}')">📥 Restock</button>
                        <button class="btn btn-secondary btn-sm" style="color:var(--danger);" onclick="deleteMedicine('${m.id}', '${m.name}')">🗑️</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openAddMedicineModal() {
    document.getElementById('addMedicineForm').reset();
    openModal('modalAddMedicine');
}

async function submitAddMedicine(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('newMedName').value.trim(),
        genericName: document.getElementById('newMedGeneric').value.trim(),
        batchNumber: document.getElementById('newMedBatch').value.trim(),
        category: document.getElementById('newMedCategory').value.trim(),
        quantityInStock: parseInt(document.getElementById('newMedStock').value) || 0,
        expiryDate: document.getElementById('newMedExpiry').value,
        purchasePrice: parseFloat(document.getElementById('newMedPurchasePrice').value),
        sellingPrice: parseFloat(document.getElementById('newMedSellingPrice').value),
        manufacturer: document.getElementById('newMedManufacturer').value.trim()
    };

    try {
        const res = await fetch('/api/medicines', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Medicine registered successfully!', 'success');
            closeModal('modalAddMedicine');
            loadMedicinesData();
            loadDashboard();
        } else {
            const err = await res.json();
            showToast(err.error || 'Failed to save medicine', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

function openRestockBatchModal(medId) {
    const med = allMedicines.find(m => m.id === medId);
    if (!med) return;

    document.getElementById('restockBatchForm').reset();
    document.getElementById('restockMedId').value = med.id;
    document.getElementById('restockMedNameDisplay').textContent = `${med.name} (${med.genericName || ''})`;
    document.getElementById('restockCurrentStockDisplay').textContent = med.quantityInStock;
    document.getElementById('restockQuantity').value = 25;

    openModal('modalRestockBatch');
}

async function submitRestockBatch(e) {
    e.preventDefault();
    const medId = document.getElementById('restockMedId').value;
    const payload = {
        quantityToAdd: parseInt(document.getElementById('restockQuantity').value),
        newBatchNumber: document.getElementById('restockBatchNo').value.trim() || null,
        newPurchasePrice: document.getElementById('restockNewPurchasePrice').value ? parseFloat(document.getElementById('restockNewPurchasePrice').value) : null,
        newSellingPrice: document.getElementById('restockNewSellingPrice').value ? parseFloat(document.getElementById('restockNewSellingPrice').value) : null,
        newExpiryDate: document.getElementById('restockNewExpiry').value || null
    };

    try {
        const res = await fetch(`/api/medicines/${medId}/restock`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Batch restocked successfully!', 'success');
            closeModal('modalRestockBatch');
            loadMedicinesData();
            loadDashboard();
        } else {
            const err = await res.json();
            showToast(err.error || 'Failed to restock', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function deleteMedicine(id, name) {
    if (!confirm(`Are you sure you want to remove '${name}' from inventory?`)) return;
    try {
        const res = await fetch(`/api/medicines/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast(`Deleted ${name}`, 'success');
            loadMedicinesData();
            loadDashboard();
        } else {
            showToast('Failed to delete medicine', 'error');
        }
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

// ----------------- 5. CUSTOMER KHATA (CREDIT LEDGER) -----------------
async function loadBorrowersData() {
    try {
        const res = await fetch('/api/borrowers');
        if (res.ok) {
            allBorrowers = await res.json();
            renderBorrowersTable(allBorrowers);
        }
    } catch (e) {
        console.error('Failed to load borrowers', e);
    }
}

async function loadKhataData() {
    await loadBorrowersData();
    try {
        const debtRes = await fetch('/api/borrowers/total-debt');
        if (debtRes.ok) {
            const data = await debtRes.json();
            document.getElementById('khataTotalDebt').textContent = formatCurrency(data.totalOutstandingDebt);
        }
    } catch (e) {
        console.error('Failed to load total debt', e);
    }
}

function renderBorrowersTable(borrowers) {
    const tbody = document.getElementById('borrowersTableBody');
    if (!borrowers || borrowers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No customer borrower accounts registered yet.</td></tr>';
        return;
    }

    tbody.innerHTML = borrowers.map(b => {
        const debt = Number(b.outstandingBalance) || 0;
        let badge = debt > 0 ? `<span class="badge badge-danger">Unpaid: ${formatCurrency(debt)}</span>` : '<span class="badge badge-success">Clear ($0.00)</span>';

        return `
            <tr>
                <td><strong>${b.customerName}</strong></td>
                <td>${b.phoneNumber}</td>
                <td style="font-size:0.8rem; color:var(--text-secondary);">${b.address || '-'}</td>
                <td><strong style="color:${debt > 0 ? 'var(--danger)' : 'var(--success)'}; font-size:1.1rem;">${formatCurrency(debt)}</strong></td>
                <td>${badge}</td>
                <td>
                    <div style="display:flex; gap:0.4rem;">
                        <button class="btn btn-primary btn-sm" onclick="openBorrowerLedger('${b.id}')">📖 View Ledger</button>
                        <button class="btn btn-secondary btn-sm" onclick="openCreditActionModal('${b.id}', 'credit')">+ Credit</button>
                        <button class="btn btn-success btn-sm" onclick="openCreditActionModal('${b.id}', 'payment')">💵 Pay</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openAddBorrowerModal() {
    document.getElementById('addBorrowerForm').reset();
    openModal('modalAddBorrower');
}

async function submitAddBorrower(e) {
    e.preventDefault();
    const payload = {
        customerName: document.getElementById('newBorrowerName').value.trim(),
        phoneNumber: document.getElementById('newBorrowerPhone').value.trim(),
        email: document.getElementById('newBorrowerEmail').value.trim() || null,
        address: document.getElementById('newBorrowerAddress').value.trim() || null,
        outstandingBalance: parseFloat(document.getElementById('newBorrowerInitialDebt').value) || 0
    };

    try {
        const res = await fetch('/api/borrowers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Customer registered in Khata successfully!', 'success');
            closeModal('modalAddBorrower');
            loadKhataData();
            loadDashboard();
        } else {
            const err = await res.json();
            showToast(err.error || 'Failed to create borrower', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

async function openBorrowerLedger(borrowerId) {
    const b = allBorrowers.find(x => x.id === borrowerId);
    if (!b) return;

    activeBorrowerForLedger = b;
    document.getElementById('ledgerModalTitle').textContent = `Customer Khata Ledger: ${b.customerName}`;
    document.getElementById('ledgerModalSubtitle').textContent = `Phone: ${b.phoneNumber} | ID: ${b.id}`;
    document.getElementById('ledgerCurrentDebt').textContent = formatCurrency(b.outstandingBalance);

    try {
        const res = await fetch(`/api/borrowers/${borrowerId}/ledger`);
        if (res.ok) {
            const ledger = await res.json();
            renderLedgerTable(ledger);
            openModal('modalBorrowerLedger');
        }
    } catch (e) {
        showToast('Failed to load ledger', 'error');
    }
}

function renderLedgerTable(transactions) {
    const tbody = document.getElementById('ledgerTableBody');
    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No credit transactions on record.</td></tr>';
        return;
    }

    tbody.innerHTML = transactions.map(tx => {
        const dateStr = new Date(tx.timestamp).toLocaleString();
        const isCredit = tx.type === 'CREDIT_TAKEN';
        const typeBadge = isCredit
            ? '<span class="badge badge-danger">Credit Taken (+)</span>'
            : '<span class="badge badge-success">Repayment Made (-)</span>';

        return `
            <tr>
                <td style="font-size:0.8rem;">${dateStr}</td>
                <td>${typeBadge}</td>
                <td><strong style="color:${isCredit ? 'var(--danger)' : 'var(--success)'};">${isCredit ? '+' : '-'}${formatCurrency(tx.amount)}</strong></td>
                <td><strong>${formatCurrency(tx.runningBalance)}</strong></td>
                <td><span class="badge badge-secondary">${tx.paymentMethod || 'CASH'}</span></td>
                <td style="font-size:0.8rem; color:var(--text-secondary);">${tx.note || '-'}</td>
            </tr>
        `;
    }).join('');
}

function openIssueCreditFromLedger() {
    if (!activeBorrowerForLedger) return;
    closeModal('modalBorrowerLedger');
    openCreditActionModal(activeBorrowerForLedger.id, 'credit');
}

function openRecordPaymentFromLedger() {
    if (!activeBorrowerForLedger) return;
    closeModal('modalBorrowerLedger');
    openCreditActionModal(activeBorrowerForLedger.id, 'payment');
}

function openCreditActionModal(borrowerId, actionType) {
    const b = allBorrowers.find(x => x.id === borrowerId);
    if (!b) return;

    document.getElementById('creditActionForm').reset();
    document.getElementById('creditActionBorrowerId').value = b.id;
    document.getElementById('creditActionType').value = actionType;

    const title = document.getElementById('creditActionTitle');
    const submitBtn = document.getElementById('creditActionSubmitBtn');

    if (actionType === 'credit') {
        title.textContent = `Issue Credit to ${b.customerName}`;
        submitBtn.textContent = 'Add Credit to Khata';
        submitBtn.className = 'btn btn-primary';
        document.getElementById('creditActionMethod').value = 'CREDIT';
        document.getElementById('creditActionNote').placeholder = 'e.g. Medicines given on credit';
    } else {
        title.textContent = `Record Repayment from ${b.customerName}`;
        submitBtn.textContent = 'Record Cash/UPI Payment';
        submitBtn.className = 'btn btn-success';
        document.getElementById('creditActionMethod').value = 'CASH';
        document.getElementById('creditActionNote').placeholder = 'e.g. Cleared bill via UPI/Cash';
    }

    openModal('modalCreditAction');
}

async function submitCreditAction(e) {
    e.preventDefault();
    const borrowerId = document.getElementById('creditActionBorrowerId').value;
    const actionType = document.getElementById('creditActionType').value;
    const payload = {
        amount: parseFloat(document.getElementById('creditActionAmount').value),
        paymentMethod: document.getElementById('creditActionMethod').value,
        note: document.getElementById('creditActionNote').value.trim()
    };

    const endpoint = actionType === 'credit'
        ? `/api/borrowers/${borrowerId}/credit`
        : `/api/borrowers/${borrowerId}/payment`;

    try {
        const res = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast(`Transaction recorded successfully!`, 'success');
            closeModal('modalCreditAction');
            loadKhataData();
            loadDashboard();
        } else {
            const err = await res.json();
            showToast(err.error || 'Transaction failed', 'error');
        }
    } catch (err) {
        showToast('Error: ' + err.message, 'error');
    }
}

// ----------------- 6. PROFIT & LOSS (P&L) -----------------
async function loadPnlData() {
    const days = document.getElementById('pnlDaysFilter').value;
    let url = '/api/analytics/pnl';
    if (days) url += `?days=${days}`;

    try {
        const res = await fetch(url);
        if (res.ok) {
            const pnl = await res.json();
            document.getElementById('pnlRevenue').textContent = formatCurrency(pnl.totalRevenue);
            document.getElementById('pnlCogs').textContent = formatCurrency(pnl.totalCogs);
            document.getElementById('pnlProfit').textContent = formatCurrency(pnl.grossProfit);
            document.getElementById('pnlMargin').textContent = `${pnl.grossMarginPercentage}%`;
            document.getElementById('pnlReceivables').textContent = formatCurrency(pnl.pendingReceivables);
            document.getElementById('pnlInventoryCost').textContent = formatCurrency(pnl.inventoryValueAtCost);
            document.getElementById('pnlInventoryRetail').textContent = `Retail Potential: ${formatCurrency(pnl.inventoryValueAtRetail)}`;
        }

        const salesRes = await fetch('/api/billing/sales');
        if (salesRes.ok) {
            const sales = await salesRes.json();
            renderPnlSalesTable(sales);
        }
    } catch (e) {
        console.error('Failed to load P&L data', e);
    }
}

function renderPnlSalesTable(sales) {
    const tbody = document.getElementById('pnlSalesTableBody');
    if (!sales || sales.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No sales data available.</td></tr>';
        return;
    }

    tbody.innerHTML = sales.map(s => {
        const rev = Number(s.totalRevenue);
        const profit = Number(s.grossProfit);
        const marginPct = rev > 0 ? ((profit / rev) * 100).toFixed(1) : '0.0';
        const dateStr = new Date(s.saleDate).toLocaleDateString();

        return `
            <tr>
                <td><strong>${s.invoiceNumber || s.id}</strong></td>
                <td>${s.medicineName}</td>
                <td>${s.quantitySold}</td>
                <td>${formatCurrency(s.purchasePricePerUnit)}</td>
                <td>${formatCurrency(s.sellingPricePerUnit)}</td>
                <td><strong>${formatCurrency(s.totalRevenue)}</strong></td>
                <td>${formatCurrency(s.cogs)}</td>
                <td style="color:var(--success); font-weight:700;">${formatCurrency(s.grossProfit)}</td>
                <td><span class="badge badge-success">${marginPct}%</span></td>
                <td style="font-size:0.8rem; color:var(--text-secondary);">${dateStr}</td>
            </tr>
        `;
    }).join('');
}

// ----------------- 7. RESTOCK ESTIMATOR -----------------
async function loadRestockData() {
    const days = document.getElementById('restockDaysSelect').value || 30;
    try {
        const res = await fetch(`/api/analytics/restock-estimates?days=${days}`);
        if (res.ok) {
            const recs = await res.json();
            renderRestockTable(recs);
        }
    } catch (e) {
        console.error('Failed to load restock estimates', e);
    }
}

function renderRestockTable(recs) {
    const tbody = document.getElementById('restockTableBody');
    if (!recs || recs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" style="text-align:center; padding:1.5rem; color:var(--text-muted);">No medicines to estimate.</td></tr>';
        return;
    }

    tbody.innerHTML = recs.map(r => {
        let urgencyBadge = '<span class="badge badge-success">Optimal</span>';
        if (r.urgency === 'CRITICAL_OUT_OF_STOCK') {
            urgencyBadge = '<span class="badge badge-danger">Critical Out of Stock</span>';
        } else if (r.urgency === 'LOW_STOCK') {
            urgencyBadge = '<span class="badge badge-warning">Low Stock (&lt;10)</span>';
        } else if (r.urgency === 'REORDER_RECOMMENDED') {
            urgencyBadge = '<span class="badge badge-info">Reorder Needed</span>';
        }

        let buyAction = '';
        if (r.recommendedPurchaseQty > 0 || r.currentStock < 10) {
            buyAction = `<button class="btn btn-primary btn-sm" onclick="openPrefilledRestock('${r.medicineId}', ${r.recommendedPurchaseQty > 0 ? r.recommendedPurchaseQty : 25})">📥 Order & Restock</button>`;
        } else {
            buyAction = `<button class="btn btn-secondary btn-sm" onclick="openPrefilledRestock('${r.medicineId}', 10)">+ Restock</button>`;
        }

        return `
            <tr>
                <td>
                    <strong>${r.medicineName}</strong>
                    <div style="font-size:0.75rem; color:var(--text-secondary);">${r.genericName || '-'}</div>
                </td>
                <td><strong style="color:${r.currentStock < 10 ? 'var(--danger)' : 'var(--text-primary)'};">${r.currentStock}</strong></td>
                <td>${r.totalSoldInPeriod}</td>
                <td><code>${r.avgDailySales.toFixed(2)} /day</code></td>
                <td>${r.projected30DayDemand.toFixed(1)}</td>
                <td>${r.bufferStock.toFixed(1)}</td>
                <td><strong style="font-size:1.05rem; color:${r.recommendedPurchaseQty > 0 ? 'var(--primary)' : 'var(--text-muted)'};">${r.recommendedPurchaseQty} units</strong></td>
                <td>${formatCurrency(r.unitPurchasePrice)}</td>
                <td style="font-weight:700; color:var(--text-primary);">${formatCurrency(r.estimatedReorderCost)}</td>
                <td>${urgencyBadge}</td>
                <td>${buyAction}</td>
            </tr>
        `;
    }).join('');
}

function openPrefilledRestock(medId, recommendedQty) {
    const med = allMedicines.find(m => m.id === medId);
    if (!med) return;

    openRestockBatchModal(medId);
    document.getElementById('restockQuantity').value = recommendedQty > 0 ? recommendedQty : 20;
}
