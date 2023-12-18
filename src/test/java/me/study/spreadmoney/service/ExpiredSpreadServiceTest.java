package me.study.spreadmoney.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.exception.UnrelatedWithClientRequestException;
import me.study.spreadmoney.repository.ExpiredSpreadRepository;
import me.study.spreadmoney.repository.SpreadDetailRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class ExpiredSpreadServiceTest {
    @Autowired
    SpreadService spreadService;
    @Autowired
    ExpiredSpreadService expiredSpreadService;
    @Autowired
    SpreadRepository spreadRepository;
    @Autowired
    SpreadDetailRepository spreadDetailRepository;
    @Autowired
    ExpiredSpreadRepository expiredSpreadRepository;
    @PersistenceContext
    EntityManager em;

    private Spread setUpExpiredSpread;

    @BeforeEach
    public void setUpForTest() {
        int userId = 1;
        String roomId = "testRoom";

        //만료된 데이터 생성
        LocalDateTime now = LocalDateTime.now();
        Spread viewableExpireSpread = Spread.createSpread(
                "abc", userId, roomId,
                10000, 8, 10000, 8,
                now, now.plusMinutes(10), now);
        setUpExpiredSpread = spreadRepository.save(viewableExpireSpread);

        em.flush();
        em.clear();
    }
    @DisplayName("Spread -> ExpireSpread")
    @Test
    void spreadToExpire(){
        //given
        String token = setUpExpiredSpread.getToken();

        //when
        Spread spread = spreadRepository.findByToken(token).get();
        expiredSpreadService.spreadToExpire(spread.getId());
        long expiredCount = expiredSpreadRepository.count();

        //then
        assertThat(expiredCount).isEqualTo(1);
    }
    @DisplayName("유효하지 않는 Spread Id")
    @Test
    void invalidTokenSpreadToExpire(){
        //then
        UnrelatedWithClientRequestException unrelatedWithClientRequestException = assertThrows(
                UnrelatedWithClientRequestException.class,
                () -> expiredSpreadService.spreadToExpire(123L));

        //checkMsg
        System.err.println(unrelatedWithClientRequestException.getMessage());
    }
}