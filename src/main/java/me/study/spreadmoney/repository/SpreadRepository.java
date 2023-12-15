package me.study.spreadmoney.repository;

import me.study.spreadmoney.entity.Spread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpreadRepository extends JpaRepository<Spread, Long> {
    int countByToken(String token);

    Optional<Spread> findByToken(String token);
}
