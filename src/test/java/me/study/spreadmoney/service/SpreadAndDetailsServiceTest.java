package me.study.spreadmoney.service;

import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetails;
import me.study.spreadmoney.repository.SpreadDetailsRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SpreadAndDetailsServiceTest {

    @Autowired SpreadAndDetailsService spreadAndDetailsService;
    @Autowired SpreadRepository spreadRepository;
    @Autowired SpreadDetailsRepository spreadDetailsRepository;

//    @DisplayName("뿌릴 돈 난수 생성")
//    @ParameterizedTest
//    @CsvSource({
//            "100, 5",
//            "200, 10",
//            "300, 15"
//    })
//    void makeRandomSpreadMoney(int totalAmount, int totalPeopleNum){
//        //given - parameter
//
//        int i = 0;
//        while (i < 100) { //100회 테스트
//            i++;
//
//            //when
//            List<Integer> list = spreadAndHistoryService.makeRandomSpreadMoney(totalAmount, totalPeopleNum);
//            int sum = 0;
//            for (Integer integer : list) {
//                sum += integer;
//            }
//            int size = list.size();
//
//            //check msg
//            String checkMsg = String.format("난수 배열: %s, 난수 합: %d", list.toString(), sum);
//            System.out.println(checkMsg);
//
//            //then
//            assertThat(totalAmount).isEqualTo(sum);
//            assertThat(totalPeopleNum).isEqualTo(size);
//        }
//    }

    @DisplayName("토큰 문자열 생성 테스트")
    @Test
    void makeToken(){
        //given
        String randomStrRegex = "[a-zA-Z0-9]{3}";

        //when
        String token = spreadAndDetailsService.makeToken();

        //check msg
        String checkMsg = String.format("Token: %s", token);
        System.out.println(checkMsg);

        //then
        assertThat(token).matches(randomStrRegex);
    }
//    @DisplayName("토큰 문자열 중복 확인 테스트")
//    @Test
//    void makeDuplicateToken(){
//        //given
//        String duplicatedToken = "abc";
//        Spread.createSpread(duplicatedToken, 1, "1", 100, 5, 100, 5, LocalDateTime.now(), LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusWeeks(1));
//        String randomStrRegex = "[a-zA-Z0-9]{3}";
//
//        //when
//        String token = spreadAndHistoryService.makeToken();
//
//        //check msg
//        String checkMsg = String.format("Token: %s", token);
//        System.out.println(checkMsg);
//
//        //then
//        assertThat(token).matches(randomStrRegex);
//    }

    @DisplayName("돈 뿌리기 통합 테스트")
    @Test
    @Rollback(value = false)
    void spreadMoney(){
        //given
        int userId = 1;
        String roomId = "a";
        int totalAmount = 100;
        int totalPeopleNum = 5;

        //when
        int i = 0;
        while (i < 100) { //100회 테스트
            String token = spreadAndDetailsService.spreadMoney(userId, roomId, totalAmount, totalPeopleNum);
            Spread byToken = spreadRepository.findByToken(token).get();

            //check msg
            System.out.println(byToken.toString());

            //then
            assertThat(token).isEqualTo(byToken.getToken());
            i++;
        }
    }

    @DisplayName("돈 받기 통합 테스트")
    @Test
    @Rollback(value = false)
    void receiveMoney(){
        //given
        int userId = 1;
        String roomId = "a";
        int totalAmount = 100;
        int totalPeopleNum = 5;
        String token = spreadAndDetailsService.spreadMoney(userId, roomId, totalAmount, totalPeopleNum);

        int[] receiveUsers = {2, 3, 4, 5, 6};
        int i = 0;
        while (i < totalPeopleNum) { //인원수만큼 테스트
            int receiveUserId = receiveUsers[i];
            //when
            int distributedAmount = spreadAndDetailsService.receiveMoney(receiveUserId, roomId, token);

            //check msg
            System.out.println(distributedAmount);

            //then
            Spread spread = spreadRepository.findByToken(token).get();
            System.out.println(spread.toString());
            List<SpreadDetails> spreadDetails = spread.getSpreadDetails();
            for (SpreadDetails spreadDetail : spreadDetails) {
                System.out.println(spreadDetail.toString());
            }
            i++;
        }
    }
}