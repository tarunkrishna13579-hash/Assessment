package com.azentio.sleeper.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TimelineEventResponse(
        String eventId,
        String accountNumber,
        String branchCode,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String channel,
        String staffId,
        Boolean kycRefreshed,
        LocalDateTime occurredAt,
        BigDecimal runningDebitTotal
) {
}