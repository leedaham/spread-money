package me.study.spreadmoney.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.repository.ExpiredSpreadRepository;
import me.study.spreadmoney.repository.SpreadDetailRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import me.study.spreadmoney.service.ReceiveService;
import me.study.spreadmoney.service.SpreadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SchedulerTest {

    @Autowired
    Scheduler scheduler;
    @Autowired
    SpreadService spreadService;
    @Autowired
    ReceiveService receiveService;
    @Autowired
    SpreadRepository spreadRepository;
    @Autowired
    SpreadDetailRepository spreadDetailRepository;
    @Autowired
    ExpiredSpreadRepository expiredSpreadRepository;
    @PersistenceContext
    EntityManager em;

    private int expiredSpreadNum = 0;
    private int normalSpreadNum = 0;

    @BeforeEach
    public void setUpForTest() {
        int userId = 1;
        String roomId = "testRoom";

        //만료된 데이터 생성
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            Spread viewableExpireSpread = Spread.createSpread(
                    "ab"+i, userId, roomId,
                    10000, 8, 10000, 8,
                    now, now.plusMinutes(10), now);
            spreadRepository.save(viewableExpireSpread);
            expiredSpreadNum += 1;
        }

        //만료되지 않은 데이터 생성
        for (int i = 0; i < 10; i++) {
            spreadService.spreadMoney(userId, roomId, 10000, 6);
            normalSpreadNum += 1;
        }

        em.flush();
        em.clear();
    }

    @DisplayName("뿌리기 만료 스케쥴러 통합 테스트")
    @Test
    void schedulerTest(){
        //when
        scheduler.spreadToExpireSpread();
        long normalCount = spreadRepository.count();
        long expiredCount = expiredSpreadRepository.count();

        //then
        assertThat(normalCount).isEqualTo(normalSpreadNum);
        assertThat(expiredCount).isEqualTo(expiredSpreadNum);
    }
}