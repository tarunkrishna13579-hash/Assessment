
package com.azentio.sleeper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "fraud_alerts",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_fraud_alert_episode",
               columnNames = "episode_key"
           )
       })
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "episode_key", nullable = false)
    private String episodeKey;

    private String rule;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "reactivated_at")
    private LocalDateTime reactivatedAt;

    @Column(name = "balance_at_reactivation",
            precision = 19, scale = 2)
    private BigDecimal balanceAtReactivation;

    @Column(name = "drained_amount", precision = 19, scale = 2)
    private BigDecimal drainedAmount;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    protected FraudAlert() {
    }

    public FraudAlert(String episodeKey,
                      String rule,
                      String accountNumber,
                      String branchCode,
                      String staffId,
                      LocalDateTime reactivatedAt,
                      BigDecimal balanceAtReactivation,
                      BigDecimal drainedAmount,
                      LocalDateTime createdAt) {

        this.episodeKey = episodeKey;
        this.rule = rule;
        this.accountNumber = accountNumber;
        this.branchCode = branchCode;
        this.staffId = staffId;
        this.reactivatedAt = reactivatedAt;
        this.balanceAtReactivation = balanceAtReactivation;
        this.drainedAmount = drainedAmount;
        this.status = "OPEN";
        this.createdAt = createdAt;
        this.lastSeenAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEpisodeKey() {
        return episodeKey;
    }

    public String getRule() {
        return rule;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getStaffId() {
        return staffId;
    }

    public LocalDateTime getReactivatedAt() {
        return reactivatedAt;
    }

    public BigDecimal getBalanceAtReactivation() {
        return balanceAtReactivation;
    }

    public BigDecimal getDrainedAmount() {
        return drainedAmount;
    }

    public void setDrainedAmount(BigDecimal drainedAmount) {
        this.drainedAmount = drainedAmount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void updateDrainAmount(
            BigDecimal drainedAmount,
            LocalDateTime lastSeenAt) {

        this.drainedAmount = drainedAmount;
        this.lastSeenAt = lastSeenAt;
    }
}
