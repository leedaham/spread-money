package me.study.spreadmoney.repository;

import me.study.spreadmoney.entity.ExpiredSpread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

/**
 * ExpireSpread Repository
 */
public interface ExpiredSpreadRepository extends JpaRepository<ExpiredSpread, Long> {
    ExpiredSpread findBySpreadUserIdAndSpreadDateTime(int userId, LocalDateTime spreadDateTime);
}
