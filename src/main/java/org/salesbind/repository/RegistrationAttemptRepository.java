package org.salesbind.repository;

import org.salesbind.model.RegistrationAttempt;
import java.util.Optional;

public interface RegistrationAttemptRepository {

    void save(RegistrationAttempt attempt);

    Optional<RegistrationAttempt> findById(String sessionId);

    Optional<RegistrationAttempt> findByEmail(String email);

    void delete(RegistrationAttempt attempt);
}
