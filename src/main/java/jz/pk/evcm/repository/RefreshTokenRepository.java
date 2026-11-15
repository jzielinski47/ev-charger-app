package jz.pk.evcm.repository;

import jz.pk.evcm.entity.RefreshToken;
import jz.pk.evcm.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.id = :id")
    int deleteByIdReturningCount(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
    int deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user = :user AND t.expiresAt <= :now")
    int deleteExpiredByUser(@Param("user") User user, @Param("now") Instant now);
}
