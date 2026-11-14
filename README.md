# Assessment
Azentio- Assessment



## 1. Project Overview

Project Sleeper is a Spring Boot-based banking fraud detection system that identifies suspicious activities in dormant accounts after reactivation.

## 2. Technology Stack

- Java 17
- Spring Boot 3.5.6
- Spring Data JPA and Hibernate
- PostgreSQL 18.7
- Spring Security with HTTP Basic Authentication
- Maven

## 3. Implemented Features

- REST API for ingesting banking events: `POST /api/core-events`.
- Automatic account creation when the first event is received.
- Account lifecycle management: ACTIVE and DORMANT.
- Idempotent event processing using unique event IDs.
- Post-reactivation debit monitoring within a configurable 30-day window.
- Fraud detection when cumulative debits reach 80% of the reactivation balance, provided the balance is at least ₹50,000.
- KYC fraud detection when an account is reactivated without KYC refresh.
- Prevention of duplicate OPEN alerts within the same reactivation episode.
- Alert retrieval through `GET /api/alerts`, with account-number masking.
- Account transaction timeline through `GET /api/accounts/{accountNumber}/timeline`, including running debit totals.

## 4. Database Tables

- `accounts` – Stores account status and balance information.
- `banking_events` – Stores incoming banking events.
- `fraud_alerts` – Stores generated fraud alerts.

## 5. Authentication

- INGEST role: Can submit banking events.
- INVESTIGATOR role: Can retrieve fraud alerts and account timelines.


## 6. How to Run

Run `ProjectSleeperApplication.java` from Eclipse. The application uses port 8084 by default
