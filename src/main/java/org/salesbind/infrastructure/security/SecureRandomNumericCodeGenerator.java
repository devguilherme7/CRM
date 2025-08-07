package org.salesbind.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;

@ApplicationScoped
public class SecureRandomNumericCodeGenerator implements RandomNumericCodeGenerator {

    private static final String DIGITS = "0123456789";
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final int idx = random.nextInt(DIGITS.length());
            code.append(DIGITS.charAt(idx));
        }
        return code.toString();
    }
}
