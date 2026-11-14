
package com.azentio.sleeper.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azentio.sleeper.dto.CoreEventRequest;
import com.azentio.sleeper.model.Account;
import com.azentio.sleeper.model.FraudAlert;
import com.azentio.sleeper.repository.FraudAlertRepository;

@Service
public class FraudDetectionService {

    private static final BigDecimal DRAIN_RATIO =
            new BigDecimal("0.80");

    private static final BigDecimal MIN_BALANCE =
            new BigDecimal("50000");

    private static final int DRAIN_WINDOW_DAYS = 30;

    private final FraudAlertRepository alertRepository;

    public FraudDetectionService(
            FraudAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional
    public void checkKycRule(
            Account account,
            CoreEventRequest event) {

        if (!Boolean.FALSE.equals(event.getKycRefreshed())) {
            return;
        }

        String key = createAlertKey(
                account, "REACTIVATION_WITHOUT_KYC");

        if (alertRepository.findByEpisodeKey(key).isPresent()) {
            return;
        }

        FraudAlert alert = new FraudAlert(
                key,
                "REACTIVATION_WITHOUT_KYC",
                account.getAccountNumber(),
                account.getBranchCode(),
                account.getStaffId(),
                account.getReactivatedAt(),
                account.getBalanceAtReactivation(),
                BigDecimal.ZERO,
                event.getOccurredAt()
        );

        alertRepository.save(alert);
    }

    @Transactional
    public void checkDrainRule(
            Account account,
            CoreEventRequest event) {

        LocalDateTime reactivatedAt =
                account.getReactivatedAt();

        if (reactivatedAt == null) {
            return;
        }

        LocalDateTime windowEnd =
                reactivatedAt.plusDays(DRAIN_WINDOW_DAYS);

        if (event.getOccurredAt().isBefore(reactivatedAt)
                || event.getOccurredAt().isAfter(windowEnd)) {
            return;
        }

        BigDecimal initialBalance =
                account.getBalanceAtReactivation();

        if (initialBalance == null
                || initialBalance.compareTo(MIN_BALANCE) < 0) {
            return;
        }

        BigDecimal threshold =
                initialBalance.multiply(DRAIN_RATIO);

        BigDecimal drainedAmount =
                account.getDrainedAmount();

        if (drainedAmount.compareTo(threshold) < 0) {
            return;
        }

        String key = createAlertKey(
                account, "POST_REACTIVATION_DRAIN");

        FraudAlert alert = alertRepository
                .findByEpisodeKey(key)
                .orElse(null);

        if (alert != null) {

            alert.updateDrainAmount(
                    drainedAmount,
                    event.getOccurredAt()
            );

        } else {

            alert = new FraudAlert(
                    key,
                    "POST_REACTIVATION_DRAIN",
                    account.getAccountNumber(),
                    account.getBranchCode(),
                    account.getStaffId(),
                    reactivatedAt,
                    initialBalance,
                    drainedAmount,
                    event.getOccurredAt()
            );
        }

        alertRepository.save(alert);
    }

    private String createAlertKey(
            Account account, String rule) {

        return account.getAccountNumber()
                + "|" + rule
                + "|" + account.getReactivatedAt();
    }

    @Transactional(readOnly = true)
    public List<FraudAlert> getAllAlerts() {
        return alertRepository.findAll();
    }
}
