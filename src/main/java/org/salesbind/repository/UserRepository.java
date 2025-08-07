package org.salesbind.repository;

import org.salesbind.model.User;

public interface UserRepository {

    void save(User user);

    boolean existsByEmail(String email);
}
