package org.salesbind.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import org.salesbind.infrastructure.configuration.RegistrationProperties;
import org.salesbind.model.RegistrationAttempt;
import java.util.Optional;

@ApplicationScoped
public class RedisRegistrationAttemptRepository implements RegistrationAttemptRepository {

    private static final String SIGNUP_SESSION_PREFIX = "signup:session:";
    private static final String SIGNUP_EMAIL_INDEX_PREFIX = "signup:email:";

    private final RedisDataSource redis;
    private final ValueCommands<String, String> valueCommands;
    private final ObjectMapper objectMapper;
    private final RegistrationProperties registrationProperties;

    public RedisRegistrationAttemptRepository(RedisDataSource redis, ObjectMapper objectMapper,
            RegistrationProperties registrationProperties) {
        this.redis = redis;
        this.valueCommands = redis.value(String.class);
        this.objectMapper = objectMapper;
        this.registrationProperties = registrationProperties;
    }

    @Override
    public void save(RegistrationAttempt attempt) {
        String sessionKey = getSignupSessionKey(attempt.getId());
        String emailIndexKey = getSignupEmailIndexKey(attempt.getEmail());

        long expirationSeconds = registrationProperties.session().expirationSeconds();
        try {
            String json = objectMapper.writeValueAsString(attempt);
            redis.withTransaction(tx -> {
                tx.value(String.class).setex(sessionKey, expirationSeconds, json);
                tx.value(String.class).setex(emailIndexKey, expirationSeconds, attempt.getId());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<RegistrationAttempt> findById(String sessionId) {
        String sessionKey = getSignupSessionKey(sessionId);
        String json = valueCommands.get(sessionKey);
        if (json == null) {
            return Optional.empty();
        }

        try {
            RegistrationAttempt attempt = objectMapper.readValue(json, RegistrationAttempt.class);
            return Optional.ofNullable(attempt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<RegistrationAttempt> findByEmail(String email) {
        String emailIndexKey = getSignupEmailIndexKey(email);
        String sessionId = valueCommands.get(emailIndexKey);
        if (sessionId == null) {
            return Optional.empty();
        }
        return findById(sessionId);
    }

    @Override
    public void delete(RegistrationAttempt attempt) {
        String registrationSessionKey = getSignupSessionKey(attempt.getId());
        String registrationEmailIndexKey = getSignupEmailIndexKey(attempt.getEmail());

        redis.key().del(registrationSessionKey, registrationEmailIndexKey);
    }

    private static String getSignupSessionKey(String sessionId) {
        return SIGNUP_SESSION_PREFIX + sessionId;
    }

    private static String getSignupEmailIndexKey(String email) {
        return SIGNUP_EMAIL_INDEX_PREFIX + email;
    }
}
