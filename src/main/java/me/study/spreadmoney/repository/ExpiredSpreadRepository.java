package me.study.spreadmoney.repository;

import me.study.spreadmoney.entity.ExpiredSpread;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpiredSpreadRepository extends JpaRepository<ExpiredSpread, Long> {
}
