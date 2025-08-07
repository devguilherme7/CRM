package org.salesbind.infrastructure.session;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class UUIDSessionIdGenerator implements SessionIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
