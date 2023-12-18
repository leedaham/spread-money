package me.study.spreadmoney.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@Transactional
class SpreadServiceTest {

    @MockBean
    private Scheduler scheduler;
    @Autowired SpreadService spreadService;
    @Autowired SpreadRepository spreadRepository;
    @Autowired SpreadDetailRepository spreadDetailRepository;
    @PersistenceContext EntityManager em;

    private Spread manualSetUpSpread;
    private Spread autoSetUpSpread;
    private int setUpSpreadNum;

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
        setUpSpreadNum += 1;

        //SpreadMoney 로 Spread, SpreadDetail 생성
        String token = spreadService.spreadMoney(userId, roomId, 10000, 6);
        autoSetUpSpread = spreadRepository.findByToken(token).get();
        setUpSpreadNum += 1;

        em.flush();
        em.clear();
    }

    @DisplayName("돈 뿌리기 통합 테스트")
    @Test
    void spreadMoney(){
        //given
        int userId = 0;
        String roomId = "room";
        int totalMoney = 10000;
        int totalPeopleNum = 5;

        //when
        String token = spreadService.spreadMoney(userId, roomId, totalMoney, totalPeopleNum);
        Optional<Spread> findByToken = spreadRepository.findByToken(token);

        //then
        assertThat(token).isEqualTo(findByToken.get().getToken());

        //checkMsg
        System.out.println("findByToken = " + findByToken);
    }

    @DisplayName("뿌린 금액과 뿌려진 돈의 합 확인")
    @Test
    void checkSpreadDetailsSumValue(){
        //given (setUpForTest)

        //when
        Spread spread = spreadRepository.findById(autoSetUpSpread.getId()).get();
        int totalMoney = spread.getTotalMoney();
        List<SpreadDetail> spreadDetails = spread.getSpreadDetails();
        int sum = spreadDetails
                .stream()
                .mapToInt(SpreadDetail::getDistributedMoney)
                .sum();

        //then
        assertThat(totalMoney).isEqualTo(sum);

        //checkMsg
        System.out.println("totalMoney = " + totalMoney);
        System.out.println("sum = " + sum);
    }

    @DisplayName("뿌릴 인원과 뿌려진 돈의 수 확인")
    @Test
    void checkSpreadDetailsDetailsCount(){
        //given (setUpForTest)

        //when
        Spread spread = spreadRepository.findById(autoSetUpSpread.getId()).get();
        List<SpreadDetail> spreadDetails = spread.getSpreadDetails();
        int totalPeopleNum = spread.getTotalPeopleNum();
        int count = spreadDetails.size();

        List<SpreadDetail> spreadDetailList = spreadDetailRepository.findBySpread(spread);
        int numOfSpreadDetail = spreadDetailList.size();

        //then
        assertThat(totalPeopleNum).isEqualTo(count);
        assertThat(totalPeopleNum).isEqualTo(numOfSpreadDetail);

        //checkMsg
        System.out.println("totalPeopleNum = " + totalPeopleNum);
        System.out.println("count = " + count);
        System.out.println("numOfSpreadDetail = " + numOfSpreadDetail);
    }

    @DisplayName("중복된 토큰으로 뿌릴 수 없음")
    @Test
    void saveDuplicateTokenSpread(){
        Spread testSpread = null;
        try {
            //given
            LocalDateTime now = LocalDateTime.now();
            Spread spread = Spread.createSpread(
                    manualSetUpSpread.getToken(), 1, "test",
                    10000, 8, 10000, 8,
                    now, now.plusMinutes(10), now.plusWeeks(1));
            //when
            Spread save = spreadRepository.save(spread);

            em.flush();
            em.clear();

            testSpread = spreadRepository.findById(save.getId()).get();

        } catch (Exception e) {
            //checkMsg
            System.out.println(e.getMessage());
        }
        assertThat(testSpread).isNull();
    }

    @DisplayName("Spread 데이터 총 개수 구하기")
    @Test
    void getAllCount(){
        //when
        int allCount = spreadService.getAllCount();

        //then
        assertThat(allCount).isEqualTo(setUpSpreadNum);
        //checkMsg
        System.out.println("allCount = " + allCount);
    }

}