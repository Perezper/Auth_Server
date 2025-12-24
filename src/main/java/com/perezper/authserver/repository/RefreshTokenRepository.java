package com.perezper.authserver.repository;

import com.perezper.authserver.entity.RefreshToken;
import com.perezper.authserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
