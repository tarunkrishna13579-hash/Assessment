package com.azentio.sleeper.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.azentio.sleeper.model.FraudAlert;
import com.azentio.sleeper.util.AccountNumberMasker;

public record AlertResponse(
        Long id,
        String episodeKey,
        String rule,
        String accountNumber,
        String branchCode,
        String staffId,
        LocalDateTime reactivatedAt,
        BigDecimal balanceAtReactivation,
        BigDecimal drainedAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastSeenAt
) {

    public static AlertResponse from(FraudAlert alert) {

        return new AlertResponse(
                alert.getId(),
                AccountNumberMasker.maskEpisodeKey(
                        alert.getEpisodeKey()),
                alert.getRule(),
                AccountNumberMasker.mask(
                        alert.getAccountNumber()),
                alert.getBranchCode(),
                alert.getStaffId(),
                alert.getReactivatedAt(),
                alert.getBalanceAtReactivation(),
                alert.getDrainedAmount(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getLastSeenAt()
        );
    }
}