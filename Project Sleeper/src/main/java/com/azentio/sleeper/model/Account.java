
package com.azentio.sleeper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.azentio.sleeper.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "branch_code")
    private String branchCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "reactivated_at")
    private LocalDateTime reactivatedAt;

    @Column(name = "balance_at_reactivation", precision = 19, scale = 2)
    private BigDecimal balanceAtReactivation;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "drained_amount", precision = 19, scale = 2)
    private BigDecimal drainedAmount;

    protected Account() {
    }

    public Account(String accountNumber,
                   String branchCode,
                   BigDecimal currentBalance) {

        this.accountNumber = accountNumber;
        this.branchCode = branchCode;
        this.currentBalance = currentBalance;
        this.status = AccountStatus.ACTIVE;
        this.drainedAmount = BigDecimal.ZERO;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public LocalDateTime getReactivatedAt() {
        return reactivatedAt;
    }

    public void setReactivatedAt(LocalDateTime reactivatedAt) {
        this.reactivatedAt = reactivatedAt;
    }

    public BigDecimal getBalanceAtReactivation() {
        return balanceAtReactivation;
    }

    public void setBalanceAtReactivation(
            BigDecimal balanceAtReactivation) {
        this.balanceAtReactivation = balanceAtReactivation;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public BigDecimal getDrainedAmount() {
        return drainedAmount;
    }

    public void setDrainedAmount(BigDecimal drainedAmount) {
        this.drainedAmount = drainedAmount;
    }
}
