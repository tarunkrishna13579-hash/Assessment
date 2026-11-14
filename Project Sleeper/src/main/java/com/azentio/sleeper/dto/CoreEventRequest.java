package com.azentio.sleeper.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.azentio.sleeper.enums.Channel;
import com.azentio.sleeper.enums.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class CoreEventRequest {

    @NotBlank
    private String eventId;

    @NotBlank
    private String accountNumber;

    @NotBlank
    private String branchCode;

    @NotNull
    private EventType type;

    private BigDecimal amount;

    @NotNull
    @PositiveOrZero
    private BigDecimal balanceAfter;

    @NotNull
    private Channel channel;

    private String staffId;

    private Boolean kycRefreshed;

    @NotNull
    private LocalDateTime occurredAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Boolean getKycRefreshed() {
        return kycRefreshed;
    }

    public void setKycRefreshed(Boolean kycRefreshed) {
        this.kycRefreshed = kycRefreshed;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}