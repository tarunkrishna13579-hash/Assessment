
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

        // Persistent duplicate check
        if (eventRepository.existsById(event.getEventId())) {
            return true;
        }

        String accountNumber = event.getAccountNumber();

        Account account = accountRepository
                .findById(accountNumber)
                .orElse(null);

        boolean newAccount = account == null;

        if (newAccount) {
            account = new Account(
                    accountNumber,
                    event.getBranchCode(),
                    event.getBalanceAfter()
            );
        }

        switch (event.getType()) {

            case DORMANCY_MARKED:

                account.setStatus(AccountStatus.DORMANT);

                account.setCurrentBalance(
                        event.getBalanceAfter()
                );

                break;

            case REACTIVATED:

                if (account.getStatus() != AccountStatus.DORMANT) {
                    throw new IllegalStateException(
                            "Cannot reactivate an account that is not dormant"
                    );
                }

                account.setStatus(AccountStatus.ACTIVE);

                account.setReactivatedAt(
                        event.getOccurredAt()
                );

                account.setBalanceAtReactivation(
                        event.getBalanceAfter()
                );

                account.setStaffId(event.getStaffId());

                account.setDrainedAmount(BigDecimal.ZERO);

                account.setCurrentBalance(
                        event.getBalanceAfter()
                );

                fraudDetectionService.checkKycRule(account, event);

                break;

            case DEBIT:

                account.setCurrentBalance(
                        event.getBalanceAfter()
                );

                if (account.getReactivatedAt() != null) {

                    boolean afterReactivation =
                            !event.getOccurredAt()
                                    .isBefore(account.getReactivatedAt());

                    boolean withinWindow =
                            !event.getOccurredAt()
                                    .isAfter(account.getReactivatedAt()
                                            .plusDays(30));

                    if (afterReactivation && withinWindow) {

                        account.setDrainedAmount(
                                account.getDrainedAmount()
                                        .add(event.getAmount())
                        );

                        fraudDetectionService.checkDrainRule(
                                account, event
                        );
                    }
                }

                break;

            case CREDIT:

                account.setCurrentBalance(
                        event.getBalanceAfter()
                );

                break;
        }

        // Persist account
        accountRepository.save(account);

        // Persist successfully processed event
        BankingEvent bankingEvent = new BankingEvent(
                event.getEventId(),
                accountNumber,
                event.getBranchCode(),
                event.getType().name(),
                event.getAmount(),
                event.getBalanceAfter(),
                String.valueOf(event.getChannel()),
                event.getStaffId(),
                event.getKycRefreshed(),
                event.getOccurredAt()
        );

        eventRepository.save(bankingEvent);

        return false;
    }

    private void validateEvent(CoreEventRequest event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Event cannot be null"
            );
        }

        if (event.getEventId() == null
                || event.getEventId().isBlank()) {
            throw new IllegalArgumentException(
                    "Event ID is required"
            );
        }

        if (event.getAccountNumber() == null
                || event.getAccountNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Account number is required"
            );
        }

        if (event.getType() == null) {
            throw new IllegalArgumentException(
                    "Event type is required"
            );
        }

        if (event.getOccurredAt() == null) {
            throw new IllegalArgumentException(
                    "Event timestamp is required"
            );
        }

        if (event.getBalanceAfter() == null
                || event.getBalanceAfter()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Valid balance is required"
            );
        }

        if (event.getType() == EventType.DEBIT
                || event.getType() == EventType.CREDIT) {

            if (event.getAmount() == null
                    || event.getAmount()
                            .compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Positive amount is required for debit and credit"
                );
            }
        }

        if (event.getType() == EventType.REACTIVATED) {

            if (event.getStaffId() == null
                    || event.getStaffId().isBlank()
                    || event.getKycRefreshed() == null) {
                throw new IllegalArgumentException(
                        "Staff ID and KYC status are required"
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return accountRepository.findById(accountNumber)
                .orElse(null);
    }
}
