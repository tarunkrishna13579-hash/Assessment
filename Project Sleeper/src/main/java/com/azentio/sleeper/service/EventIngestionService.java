
package com.azentio.sleeper.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azentio.sleeper.dto.CoreEventRequest;
import com.azentio.sleeper.enums.AccountStatus;
import com.azentio.sleeper.enums.EventType;
import com.azentio.sleeper.model.Account;
import com.azentio.sleeper.model.BankingEvent;
import com.azentio.sleeper.repository.AccountRepository;
import com.azentio.sleeper.repository.BankingEventRepository;

@Service
public class EventIngestionService {

    private final AccountRepository accountRepository;
    private final BankingEventRepository eventRepository;
    private final FraudDetectionService fraudDetectionService;

    public EventIngestionService(
            AccountRepository accountRepository,
            BankingEventRepository eventRepository,
            FraudDetectionService fraudDetectionService) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Transactional
    public boolean ingestEvent(CoreEventRequest event) {

        validateEvent(event);

        // Idempotency: an already processed event changes nothing.
        if (eventRepository.existsById(event.getEventId())) {
            return true;
        }

        Account account = accountRepository
                .findById(event.getAccountNumber())
                .orElseGet(() -> new Account(
                        event.getAccountNumber(),
                        event.getBranchCode(),
                        event.getBalanceAfter()
                ));

        switch (event.getType()) {

            case DORMANCY_MARKED:
                account.setStatus(AccountStatus.DORMANT);
                account.setCurrentBalance(event.getBalanceAfter());

                // The previous reactivation episode is no longer current.
                account.setReactivatedAt(null);
                account.setBalanceAtReactivation(null);
                account.setStaffId(null);
                account.setDrainedAmount(BigDecimal.ZERO);
                break;

            case REACTIVATED:
                if (account.getStatus() != AccountStatus.DORMANT) {
                    throw new IllegalStateException(
                            "Cannot reactivate an account that is not dormant"
                    );
                }

                account.setStatus(AccountStatus.ACTIVE);
                account.setReactivatedAt(event.getOccurredAt());
                account.setBalanceAtReactivation(event.getBalanceAfter());
                account.setStaffId(event.getStaffId());
                account.setDrainedAmount(BigDecimal.ZERO);
                account.setCurrentBalance(event.getBalanceAfter());

                fraudDetectionService.checkKycRule(account, event);
                break;

            case DEBIT:
                account.setCurrentBalance(event.getBalanceAfter());

                if (isDebitWithinCurrentEpisode(account, event)) {
                    BigDecimal currentDrained =
                            account.getDrainedAmount() == null
                                    ? BigDecimal.ZERO
                                    : account.getDrainedAmount();

                    account.setDrainedAmount(
                            currentDrained.add(event.getAmount())
                    );

                    fraudDetectionService.checkDrainRule(account, event);
                }
                break;

            case CREDIT:
                account.setCurrentBalance(event.getBalanceAfter());
                break;

            default:
                throw new IllegalArgumentException("Unsupported event type");
        }

        // Both saves participate in the same transaction.
        accountRepository.save(account);

        BankingEvent bankingEvent = new BankingEvent(
                event.getEventId(),
                event.getAccountNumber(),
                event.getBranchCode(),
                event.getType().name(),
                event.getAmount(),
                event.getBalanceAfter(),
                event.getChannel().name(),
                event.getStaffId(),
                event.getKycRefreshed(),
                event.getOccurredAt()
        );

        eventRepository.save(bankingEvent);

        return false;
    }

    private boolean isDebitWithinCurrentEpisode(
            Account account,
            CoreEventRequest event) {

        if (account.getReactivatedAt() == null) {
            return false;
        }

        return !event.getOccurredAt().isBefore(account.getReactivatedAt())
                && !event.getOccurredAt().isAfter(
                        account.getReactivatedAt().plusDays(30)
                );
    }

    private void validateEvent(CoreEventRequest event) {

        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        if (isBlank(event.getEventId())) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (isBlank(event.getAccountNumber())) {
            throw new IllegalArgumentException("Account number is required");
        }

        if (isBlank(event.getBranchCode())) {
            throw new IllegalArgumentException("Branch code is required");
        }

        if (event.getType() == null) {
            throw new IllegalArgumentException("Event type is required");
        }

        if (event.getChannel() == null) {
            throw new IllegalArgumentException("Channel is required");
        }

        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException("Event timestamp is required");
        }

        if (event.getBalanceAfter() == null
                || event.getBalanceAfter().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Non-negative balanceAfter is required"
            );
        }

        if (event.getType() == EventType.DEBIT
                || event.getType() == EventType.CREDIT) {

            if (event.getAmount() == null
                    || event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Positive amount is required for debit and credit"
                );
            }
        }

        if (event.getType() == EventType.REACTIVATED) {

            if (isBlank(event.getStaffId())) {
                throw new IllegalArgumentException(
                        "Staff ID is required for reactivation"
                );
            }

            if (event.getKycRefreshed() == null) {
                throw new IllegalArgumentException(
                        "KYC status is required for reactivation"
                );
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber).orElse(null);
    }
}
