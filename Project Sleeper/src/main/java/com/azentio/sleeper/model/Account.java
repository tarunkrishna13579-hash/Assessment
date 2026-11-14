package com.azentio.sleeper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.azentio.sleeper.enums.AccountStatus;

public class Account {

    private String accountNumber;

    private String branchCode;

    private AccountStatus status;

    private BigDecimal currentBalance;

    private LocalDateTime reactivatedAt;

    private BigDecimal balanceAtReactivation;

    private String staffId;

    private BigDecimal drainedAmount;

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