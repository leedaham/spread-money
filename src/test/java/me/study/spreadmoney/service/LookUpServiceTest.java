package me.study.spreadmoney.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.study.spreadmoney.api.dto.LookUpSpreadInfoDto;
import me.study.spreadmoney.entity.ExpiredSpread;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import me.study.spreadmoney.repository.ExpiredSpreadRepository;
import me.study.spreadmoney.repository.SpreadDetailRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import me.study.spreadmoney.scheduler.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Transactional
class LookUpServiceTest {
    @MockBean
    private Scheduler scheduler;
    @Autowired
    SpreadService spreadService;
    @Autowired
    ReceiveService receiveService;
    @Autowired
    LookUpService lookUpService;
    @Autowired
    SpreadRepository spreadRepository;
    @Autowired
    SpreadDetailRepository spreadDetailRepository;
    @Autowired
    ExpiredSpreadRepository expiredSpreadRepository;
    @PersistenceContext
    EntityManager em;

    private Spread setUpViewableExpireSpread;
    private Spread autoSetUpSpread;

    @BeforeEach
    public void setUpForTest() {
        //Scheduler method 비활성화
        doNothing().when(scheduler).spreadToExpireSpread();

        int userId = 1;
        String roomId = "testRoom";

        //Token 직접 지정, 조회 기간 만료일로 직접 설정하여 Spread 생성 및 저장(SpreadDetail 없음)
        LocalDateTime now = LocalDateTime.now();
        Spread viewableExpireSpread = Spread.createSpread(
                "abe", userId, roomId,
                10000, 8, 10000, 8,
                now, now.plusMinutes(10), now);
        spreadRepository.save(viewableExpireSpread);
        setUpViewableExpireSpread = viewableExpireSpread;

        //SpreadMoney 로 Spread, SpreadDetail 생성
        String token = spreadService.spreadMoney(userId, roomId, 10000, 6);
        autoSetUpSpread = spreadRepository.findByToken(token).get();

        em.flush();
        em.clear();
    }

    @DisplayName("뿌리기 조회 통합 테스트")
    @Test
    void lookupSpreadInfo(){
        //given
        int userId = autoSetUpSpread.getUserId();
        String roomId = autoSetUpSpread.getRoomId();
        String token = autoSetUpSpread.getToken();

        //when
        Spread spread = spreadRepository.findByToken(token).get();
        int receivedMoney = receiveService.receiveMoney(userId + 1, roomId, token);
        LookUpSpreadInfoDto lookUpSpreadInfoDto = lookUpService.lookUpSpreadInfo(userId, token);

        //then
        assertThat(spread.getSpreadDateTime()).isEqualTo(lookUpSpreadInfoDto.getSpreadDateTime());
        assertThat(receivedMoney).isEqualTo(lookUpSpreadInfoDto.getTotalReceivedMoney());

        //checkMsg
        System.out.println("lookUpSpreadInfoDto = " + lookUpSpreadInfoDto);
    }

    @DisplayName("유효하지 않는 token은 조회할 수 없음")
    @Test
    void invalidTokenLookUpSpread(){
        //given
        int userId = autoSetUpSpread.getUserId();
        String token = "!!!";

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> lookUpService.lookUpSpreadInfo(userId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("뿌린 사용자가 아니라면 조회할 수 없음")
    @Test
    void invalidUserLookUpSpread(){
        //given
        int userId = autoSetUpSpread.getUserId()+1;
        String token = autoSetUpSpread.getToken();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> lookUpService.lookUpSpreadInfo(userId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("조회가 만료된 뿌리기는 조회할 수 없음")
    @Test
    void viewableExpireLookUpSpread(){
        //given
        int userId = setUpViewableExpireSpread.getUserId();
        String token = setUpViewableExpireSpread.getToken();
        Spread spread = spreadRepository.findByToken(token).get();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> lookUpService.lookUpSpreadInfo(userId, token));

        em.flush();
        em.clear();

        ExpiredSpread expiredSpread = expiredSpreadRepository.findBySpreadUserIdAndSpreadDateTime(spread.getUserId(), spread.getSpreadDateTime());
        assertThat(spread.getUserId()).isEqualTo(expiredSpread.getSpreadUserId());
        assertThat(spread.getSpreadDateTime()).isEqualTo(expiredSpread.getSpreadDateTime());

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
        System.out.println("expiredSpread = " + expiredSpread);
    }
}