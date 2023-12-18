package me.study.spreadmoney.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.study.spreadmoney.entity.ExpiredSpread;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
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
class ReceiveServiceTest {

    @MockBean
    private Scheduler scheduler;
    @Autowired SpreadService spreadService;
    @Autowired ReceiveService receiveService;
    @Autowired
    SpreadRepository spreadRepository;
    @Autowired
    SpreadDetailRepository spreadDetailRepository;
    @Autowired
    ExpiredSpreadRepository expiredSpreadRepository;
    @PersistenceContext
    EntityManager em;

    private Spread setUpReceivableExpireSpread;
    private Spread setUpViewableExpireSpread;

    private Spread manualSetUpSpread;
    private Spread autoSetUpSpread;

    @BeforeEach
    public void setUpForTest() {
        //Scheduler method 비활성화
        doNothing().when(scheduler).spreadToExpireSpread();

        int userId = 1;
        String roomId = "testRoom";

        //Token 직접 지정하여 Spread 생성 및 저장(SpreadDetail 없음)
        LocalDateTime now = LocalDateTime.now();
        Spread manualSpread = Spread.createSpread(
                "abc", userId, roomId,
                10000, 8, 10000, 8,
                now, now.plusMinutes(10), now.plusWeeks(1));
        spreadRepository.save(manualSpread);
        manualSetUpSpread = manualSpread;

        //Token 직접 지정, 받기 기간 만료일로 직접 설정하여 Spread 생성 및 저장(SpreadDetail 없음)
        Spread receivableExpireSpread = Spread.createSpread(
                "abd", userId, roomId,
                10000, 8, 10000, 8,
                now, now, now.plusWeeks(1));
        spreadRepository.save(receivableExpireSpread);
        setUpReceivableExpireSpread = receivableExpireSpread;

        //Token 직접 지정, 조회 기간 만료일로 직접 설정하여 Spread 생성 및 저장(SpreadDetail 없음)
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

    @DisplayName("돈 받기 통합 테스트")
    @Test
    void receiveMoney(){
        //given
        int userId = autoSetUpSpread.getUserId()+1;
        String roomId = autoSetUpSpread.getRoomId();
        String token = autoSetUpSpread.getToken();

        //when
        int receivedMoney = receiveService.receiveMoney(userId, roomId, token);
        SpreadDetail findSpreadDetail = spreadDetailRepository.findByDistributedMoneyAndStatusAndReceivedUserId(
                receivedMoney,
                SpreadDetailStatus.DONE,
                userId
        );

        //then
        assertThat(receivedMoney).isEqualTo(findSpreadDetail.getDistributedMoney());

        //checkMsg
        System.out.println("receivedMoney = " + receivedMoney);
        System.out.println("findSpreadDetail = " + findSpreadDetail);
    }

    @DisplayName("자신의 뿌리기 받을 수 없음")
    @Test
    void spreadUserReceiveMoney(){
        //given
        int userId = autoSetUpSpread.getUserId();
        String roomId = autoSetUpSpread.getRoomId();
        String token = autoSetUpSpread.getToken();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("다른방 뿌리기 돈 받을 수 없음")
    @Test
    void otherRoomReceiveMoney(){
        //given
        int userId = autoSetUpSpread.getUserId()+1;
        String roomId = autoSetUpSpread.getRoomId()+1;
        String token = autoSetUpSpread.getToken();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }

    @DisplayName("이미 모두 받아간 뿌리기 받을 수 없음")
    @Test
    void alreadyReceiveMoney(){
        //given
        int userId = autoSetUpSpread.getUserId()+1;
        String roomId = autoSetUpSpread.getRoomId();
        String token = autoSetUpSpread.getToken();
        Spread spread = spreadRepository.findByToken(token).get();
        int totalPeopleNum = spread.getTotalPeopleNum();

        //when
        int alreadyReceiveUser = userId + 100;
        for (int i = 0; i < totalPeopleNum; i++) {
            receiveService.receiveMoney(alreadyReceiveUser, roomId, token);
            alreadyReceiveUser += 1;
        }
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("이미 받은 사람은 또 받을 수 없음")
    @Test
    void sameUserReceiveMoneyAgain(){
        //given
        int userId = autoSetUpSpread.getUserId()+1;
        String roomId = autoSetUpSpread.getRoomId();
        String token = autoSetUpSpread.getToken();

        receiveService.receiveMoney(userId, roomId, token);
        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("SpreadDetail 이 생성되지 않은 뿌리기는 받을 수 없음")
    @Test
    void noDetailReceiveMoney(){
        //given
        int userId = manualSetUpSpread.getUserId()+1;
        String roomId = manualSetUpSpread.getRoomId();
        String token = manualSetUpSpread.getToken();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }
    @DisplayName("유효하지 않는 token은 받을 수 없음")
    @Test
    void invalidTokenReceiveMoney(){
        //given
        int userId = manualSetUpSpread.getUserId()+1;
        String roomId = manualSetUpSpread.getRoomId();
        String token = "!!!";

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }

    @DisplayName("받기가 만료된 뿌리기는 받을 수 없음")
    @Test
    void receivableExpireReceiveMoney(){
        //given
        int userId = setUpReceivableExpireSpread.getUserId()+1;
        String roomId = setUpReceivableExpireSpread.getRoomId();
        String token = setUpReceivableExpireSpread.getToken();

        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

        //checkMsg
        System.err.println(predictableRuntimeException.getMessage());
    }

    @DisplayName("조회가 만료된 뿌리기는 받을 수 없음")
    @Test
    void viewableExpireReceiveMoney(){
        //given
        int userId = setUpViewableExpireSpread.getUserId()+1;
        String roomId = setUpViewableExpireSpread.getRoomId();
        String token = setUpViewableExpireSpread.getToken();

        Spread spread = spreadRepository.findByToken(token).get();
        //when
        PredictableRuntimeException predictableRuntimeException = assertThrows(
                PredictableRuntimeException.class,
                () -> receiveService.receiveMoney(userId, roomId, token));

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