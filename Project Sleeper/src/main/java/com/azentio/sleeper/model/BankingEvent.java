
package com.azentio.sleeper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "banking_events")
public class BankingEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "event_type", nullable = false)
    private String type;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String channel;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "kyc_refreshed")
    private Boolean kycRefreshed;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected BankingEvent() {
    }

    public BankingEvent(String eventId,
                        String accountNumber,
                        String branchCode,
                        String type,
                        BigDecimal amount,
                        BigDecimal balanceAfter,
                        String channel,
                        String staffId,
                        Boolean kycRefreshed,
                        LocalDateTime occurredAt) {

        this.eventId = eventId;
        this.accountNumber = accountNumber;
        this.branchCode = branchCode;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.channel = channel;
        this.staffId = staffId;
        this.kycRefreshed = kycRefreshed;
        this.occurredAt = occurredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getType() {
        return type;
    }
    public String getBranchCode() {
        return branchCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getChannel() {
        return channel;
    }

    public String getStaffId() {
        return staffId;
    }

    public Boolean getKycRefreshed() {
        return kycRefreshed;
    }
}
