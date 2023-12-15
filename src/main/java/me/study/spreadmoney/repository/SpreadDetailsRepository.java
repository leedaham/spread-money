package me.study.spreadmoney.repository;

import me.study.spreadmoney.entity.SpreadDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpreadDetailsRepository extends JpaRepository<SpreadDetails, Long> {
}
