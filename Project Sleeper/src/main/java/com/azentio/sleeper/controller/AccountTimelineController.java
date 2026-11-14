package com.azentio.sleeper.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.azentio.sleeper.dto.TimelineEventResponse;
import com.azentio.sleeper.model.BankingEvent;
import com.azentio.sleeper.repository.AccountRepository;
import com.azentio.sleeper.repository.BankingEventRepository;
import com.azentio.sleeper.util.AccountNumberMasker;

@RestController
@RequestMapping("/api/accounts")
public class AccountTimelineController {

    private final AccountRepository accountRepository;
    private final BankingEventRepository eventRepository;

    public AccountTimelineController(
            AccountRepository accountRepository,
            BankingEventRepository eventRepository) {

        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/{accountNumber}/timeline")
    public ResponseEntity<List<TimelineEventResponse>> getTimeline(
            @PathVariable String accountNumber) {

        // Check whether the account exists
        if (!accountRepository.existsById(accountNumber)) {
            return ResponseEntity.notFound().build();
        }

        // Fetch events in chronological order
        List<BankingEvent> events =
                eventRepository
                        .findByAccountNumberOrderByOccurredAtAsc(
                                accountNumber);

        // Find the latest dormancy event
        int latestDormancyIndex = -1;

        for (int i = 0; i < events.size(); i++) {

            if ("DORMANCY_MARKED".equals(events.get(i).getType())) {
                latestDormancyIndex = i;
            }
        }

        // Select events from latest dormancy episode
        List<BankingEvent> selectedEvents;

        if (latestDormancyIndex != -1) {

            selectedEvents = events.subList(
                    latestDormancyIndex,
                    events.size());

        } else {

            // If no dormancy event exists, return latest 50
            int startIndex = Math.max(0, events.size() - 50);

            selectedEvents = events.subList(
                    startIndex,
                    events.size());
        }

        // Calculate running debit total
        BigDecimal runningDebitTotal = BigDecimal.ZERO;

        List<TimelineEventResponse> response =
                new ArrayList<>();

        for (BankingEvent event : selectedEvents) {

            if ("DEBIT".equals(event.getType())
                    && event.getAmount() != null) {

                runningDebitTotal =
                        runningDebitTotal.add(event.getAmount());
            }

            response.add(new TimelineEventResponse(
                    event.getEventId(),
                    AccountNumberMasker.mask(
                            event.getAccountNumber()),
                    event.getBranchCode(),
                    event.getType(),
                    event.getAmount(),
                    event.getBalanceAfter(),
                    event.getChannel(),
                    event.getStaffId(),
                    event.getKycRefreshed(),
                    event.getOccurredAt(),
                    runningDebitTotal
            ));
        }

        return ResponseEntity.ok(response);
    }
}