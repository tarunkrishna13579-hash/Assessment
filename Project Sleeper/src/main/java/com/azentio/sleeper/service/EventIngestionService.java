package com.azentio.sleeper.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.azentio.sleeper.dto.CoreEventRequest;
import com.azentio.sleeper.enums.AccountStatus;
import com.azentio.sleeper.enums.EventType;
import com.azentio.sleeper.model.Account;

@Service
public class EventIngestionService {

    private final Map<String, Account> accounts = new HashMap<>();

    private final Set<String> processedEventIds = new HashSet<>();

    public synchronized boolean ingestEvent(CoreEventRequest event) {

        validateEvent(event);

        // Step 1: Check duplicate event
        if (processedEventIds.contains(event.getEventId())) {
            return true;
        }

        // Step 2: Find account
        String accountNumber = event.getAccountNumber();

        Account account = accounts.get(accountNumber);

        boolean newAccount = account == null;

        // Step 3: Create unknown account as ACTIVE
        if (newAccount) {
            account = new Account(
                accountNumber,
                event.getBranchCode(),
                event.getBalanceAfter()
            );
        }

        // Step 4: Process banking event
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
                        "Account is not dormant. Cannot reactivate."
                    );
                }

                account.setStatus(AccountStatus.ACTIVE);

                account.setReactivatedAt(
                    event.getOccurredAt()
                );

                account.setBalanceAtReactivation(
                    event.getBalanceAfter()
                );

                account.setStaffId(
                    event.getStaffId()
                );

                account.setDrainedAmount(BigDecimal.ZERO);

                account.setCurrentBalance(
                    event.getBalanceAfter()
                );

                break;

            case DEBIT:

                account.setCurrentBalance(
                    event.getBalanceAfter()
                );

                // Count only debits after latest reactivation
                if (account.getReactivatedAt() != null &&
                    !event.getOccurredAt().isBefore(
                        account.getReactivatedAt())) {

                    account.setDrainedAmount(
                        account.getDrainedAmount().add(
                            event.getAmount()
                        )
                    );
                }

                break;

            case CREDIT:

                account.setCurrentBalance(
                    event.getBalanceAfter()
                );

                break;
        }

        // Step 5: Save account only after successful processing
        if (newAccount) {
            accounts.put(accountNumber, account);
        }

        // Step 6: Record processed event ID
        processedEventIds.add(event.getEventId());

        return false;
    }

    private void validateEvent(CoreEventRequest event) {

        if (event == null) {
            throw new IllegalArgumentException(
                "Event cannot be null"
            );
        }

        if (event.getType() == EventType.DEBIT ||
            event.getType() == EventType.CREDIT) {

            if (event.getAmount() == null ||
                event.getAmount().signum() <= 0) {

                throw new IllegalArgumentException(
                    "Positive amount is required for debit and credit"
                );
            }
        }

        if (event.getType() == EventType.REACTIVATED) {

            if (event.getStaffId() == null ||
                event.getStaffId().isBlank() ||
                event.getKycRefreshed() == null) {

                throw new IllegalArgumentException(
                    "Staff ID and KYC status are required"
                );
            }
        }
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }
}