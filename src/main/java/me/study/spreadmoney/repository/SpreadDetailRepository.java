package me.study.spreadmoney.repository;

import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SpreadDetail Repository
 */
public interface SpreadDetailRepository extends JpaRepository<SpreadDetail, Long> {
    List<SpreadDetail> findBySpread(Spread spread);

    SpreadDetail findByDistributedMoneyAndStatusAndReceivedUserId(int distributedMoney, SpreadDetailStatus status, int receivedUserId);
}
