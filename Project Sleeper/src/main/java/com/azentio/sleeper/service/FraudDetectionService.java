
package com.azentio.sleeper.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azentio.sleeper.dto.CoreEventRequest;
import com.azentio.sleeper.model.Account;
import com.azentio.sleeper.model.FraudAlert;
import com.azentio.sleeper.repository.FraudAlertRepository;

@Service
public class FraudDetectionService {

    private final FraudAlertRepository alertRepository;

    private final BigDecimal drainRatio;
    private final BigDecimal minBalance;
    private final long drainWindowDays;

    public FraudDetectionService(
            FraudAlertRepository alertRepository,
            @Value("${sleeper.fraud.drain-ratio:0.80}")
            BigDecimal drainRatio,
            @Value("${sleeper.fraud.min-balance:50000}")
            BigDecimal minBalance,
            @Value("${sleeper.fraud.drain-window-days:30}")
            long drainWindowDays) {

        this.alertRepository = alertRepository;
        this.drainRatio = drainRatio;
        this.minBalance = minBalance;
        this.drainWindowDays = drainWindowDays;
    }

    @Transactional
    public void checkKycRule(
            Account account,
            CoreEventRequest event) {

        if (!Boolean.FALSE.equals(event.getKycRefreshed())) {
            return;
        }

        String rule = "REACTIVATION_WITHOUT_KYC";
        String key = createAlertKey(account, rule);

        if (alertRepository.findByEpisodeKey(key).isPresent()) {
            return;
        }

        FraudAlert alert = new FraudAlert(
                key,
                rule,
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

        LocalDateTime reactivatedAt = account.getReactivatedAt();

        if (reactivatedAt == null) {
            return;
        }

        LocalDateTime windowEnd =
                reactivatedAt.plusDays(drainWindowDays);

        if (event.getOccurredAt().isBefore(reactivatedAt)
                || event.getOccurredAt().isAfter(windowEnd)) {
            return;
        }

        BigDecimal initialBalance =
                account.getBalanceAtReactivation();

        if (initialBalance == null
                || initialBalance.compareTo(minBalance) < 0) {
            return;
        }

        BigDecimal threshold = initialBalance.multiply(drainRatio);
        BigDecimal drainedAmount = account.getDrainedAmount();

        if (drainedAmount.compareTo(threshold) < 0) {
            return;
        }

        String rule = "POST_REACTIVATION_DRAIN";
        String key = createAlertKey(account, rule);

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
                    rule,
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

    private String createAlertKey(Account account, String rule) {
        return account.getAccountNumber()
                + "|" + rule
                + "|" + account.getReactivatedAt();
    }

    @Transactional(readOnly = true)
    public List<FraudAlert> getAllAlerts() {
        return alertRepository.findAll();
    }
}
