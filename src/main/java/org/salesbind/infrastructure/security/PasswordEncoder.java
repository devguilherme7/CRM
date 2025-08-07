package org.salesbind.infrastructure.security;

public interface PasswordEncoder {

    String encode(String rawPassword);
}
