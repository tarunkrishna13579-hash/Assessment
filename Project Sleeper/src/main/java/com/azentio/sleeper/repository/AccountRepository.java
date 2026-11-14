
package com.azentio.sleeper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.azentio.sleeper.model.Account;

public interface AccountRepository
        extends JpaRepository<Account, String> {
}
