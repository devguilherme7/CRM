package org.salesbind.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.salesbind.model.User;

@ApplicationScoped
public class JpaUserRepository implements UserRepository, PanacheRepository<User> {

    @Override
    public void save(User user) {
        persist(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return count("email", email.toLowerCase().trim()) > 0;
    }
}
