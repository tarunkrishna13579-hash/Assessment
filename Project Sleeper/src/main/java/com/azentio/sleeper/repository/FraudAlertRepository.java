
package com.azentio.sleeper.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azentio.sleeper.model.FraudAlert;

public interface FraudAlertRepository
        extends JpaRepository<FraudAlert, Long> {

    Optional<FraudAlert> findByEpisodeKey(String episodeKey);
}
