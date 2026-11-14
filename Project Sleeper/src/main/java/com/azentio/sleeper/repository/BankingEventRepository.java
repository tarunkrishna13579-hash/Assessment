
package com.azentio.sleeper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azentio.sleeper.model.BankingEvent;

public interface BankingEventRepository
        extends JpaRepository<BankingEvent, String> {
}
