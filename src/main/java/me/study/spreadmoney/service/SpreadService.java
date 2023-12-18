package me.study.spreadmoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import me.study.spreadmoney.repository.SpreadDetailRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 뿌리기 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpreadService {
    private final SpreadRepository spreadRepository;
    private final SpreadDetailRepository spreadDetailRepository;
    private final Random random = new Random();

    //config.yml 설정 값
    @Value("${config.policy.spread-expire-date.unit}")
    private String spread_expire_date_unit; //받기 만료 시간 설정시 추가될 값의 단위
    @Value("${config.policy.spread-expire-date.duration}")
    private int spread_expire_date_duration; //받기 만료 시간 설정시 추가될 값
    @Value("${config.policy.viewable-date.unit}")
    private String viewable_date_unit; //조회 만료 시간 설정시 추가될 값의 단위
    @Value("${config.policy.viewable-date.duration}")
    private int viewable_date_duration; //조회 만료 시간 설정시 추가될 값의

    /**
     * 뿌리기
     * 1) 뿌릴 금액을 인원수에 맞게 분배
     * 2) token 은 고유값이며 3자리 문자열이며 예측이 불가능하게 구성
     *
     * @param userId         뿌리기 요청 사용자 ID
     * @param roomId         뿌리기 요청 대화방 ID
     * @param totalMoney     뿌릴 금액
     * @param totalPeopleNum 뿌릴 인원
     * @return 생성된 뿌리기 token 값
     */
    @Transactional
    public String spreadMoney(int userId, String roomId, int totalMoney, int totalPeopleNum) {
        //뿌린 시간 정의, 뿌린 시간을 기반으로 받기 만료 시간, 조회 만료 시간 설정
        LocalDateTime spreadDateTime = LocalDateTime.now();
        LocalDateTime receivableExpireDateTime = makeReceivableExpireDateTime(spreadDateTime);
        LocalDateTime viewableExpireDateTime = makeViewableExpireDateTime(spreadDateTime);

        //고유 token 생성
        String token = makeToken();

        //뿌릴 금액, 뿌릴 인원으로 최초 남은 금액, 남은 인원수 설정
        int remainMoney = totalMoney;
        int remainPeopleNum = totalPeopleNum;

        //뿌리기 생성 및 저장
        Spread spread = Spread.createSpread(
                token, userId, roomId,
                totalMoney, totalPeopleNum, remainMoney, remainPeopleNum,
                spreadDateTime, receivableExpireDateTime, viewableExpireDateTime);
        spreadRepository.save(spread);

        //뿌리기 세부사항 생성 및 저장(뿌릴 인원에 맞게 뿌릴 금액을 나누어 배정)
        List<Integer> distributedMoneyList = makeRandomSpreadMoney(totalMoney, totalPeopleNum);
        for (Integer distributedMoney : distributedMoneyList) {
            SpreadDetail spreadDetail = SpreadDetail.createSpreadDetails(spread, distributedMoney);
            spreadDetailRepository.save(spreadDetail);
        }

        return token;
    }

    /**
     * token 생성
     * 1) token 은 고유값이며 3자리 문자열이며 예측이 불가능하게 구성
     *
     * @return 생성된 뿌리기 token 값
     */
    private String makeToken() {
        String token = null;

        //토큰 생성 시도
        int tryNum = 0;
        while (tryNum < 1000) { // 고유 토큰 생성 실패시 1000회까지 시도
            //토큰 생성
            token = makeTokenStr();

            //토큰 Unique 확인
            boolean uniqueToken = isUniqueToken(token);
            if (uniqueToken)
                break;
            else
                token = null;
            tryNum++;
        }

        //고유 토큰 생성 실패
        if (token == null)
            throw new PredictableRuntimeException("뿌리기 요청 Token 생성에 실패했습니다. 다시 시도 해주세요. 계속해서 문제가 발생한다면 관리자에게 문의해주십시오.");

        log.info("Token 생성 완료, token: {}", token);
        return token;
    }

    /**
     * 랜덤 3자리 문자열 생성
     *
     * @return 랜덤 3자리 문자열
     */
    private String makeTokenStr() {
        String token;
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        token = sb.toString();
        return token;
    }

    /**
     * DB 조회 후 token 고유값 검증
     *
     * @param token 랜덤 3자리 문자열로 생성된 토큰 값
     * @return token 고유값 여부
     */
    private boolean isUniqueToken(String token) {
        int count = spreadRepository.countByToken(token);
        return (count == 0);
    }

    /**
     * 뿌릴 인원에 맞게 뿌릴 금액 나누기
     * 1) 나누어진 값의 합이 뿌릴 금액과 같아야 함
     * 2) 나누어진 값이 음수이거나, 0이면 안됨
     * 3) 위 조건을 충족하면서 무작위 값으로 설정
     *
     * @param totalMoney     뿌릴 금액
     * @param totalPeopleNum 뿌릴 인원
     * @return 뿌릴 인원 사이즈의 뿌릴 금액 list
     */
    private List<Integer> makeRandomSpreadMoney(int totalMoney, int totalPeopleNum) {
        List<Integer> list = new ArrayList<>();

        int remaining = totalMoney;
        for (int i = 1; i < totalPeopleNum; i++) {
            int randomMaxValue = remaining - (totalPeopleNum - i);
            randomMaxValue = (int) Math.round(randomMaxValue * 0.7); // 먼저 생성되는 난수에서 너무 큰 금액 가져가는 것 방지
            int randomNumber = random.nextInt(randomMaxValue - 1) + 1;
            list.add(randomNumber);
            remaining -= randomNumber;
        }
        list.add(remaining);

        return list;
    }

    /**
     * 뿌리기 건의 받기 만료 시간 설정
     *
     * @param spreadDateTime 받기 만료 시간의 기준이 되는 뿌린 시간
     * @return 받기 만료 시간
     */
    private LocalDateTime makeReceivableExpireDateTime(LocalDateTime spreadDateTime) {
        return makePlusLocalDateTimeWithCondition(spreadDateTime, spread_expire_date_unit, spread_expire_date_duration);
    }

    /**
     * 뿌리기 건의 조회 만료 시간 설정
     *
     * @param spreadDateTime 조회 만료 시간의 기준이 되는 뿌린 시간
     * @return 조회 만료 시간
     */
    private LocalDateTime makeViewableExpireDateTime(LocalDateTime spreadDateTime) {
        return makePlusLocalDateTimeWithCondition(spreadDateTime, viewable_date_unit, viewable_date_duration);
    }

    /**
     * 기준 시간, 추가 단위(년/월/일/시/분/초), 추가 값으로 기준 시간에서 추가된 값을 반환
     *
     * @param standardDateTime  기준 시간
     * @param unitOfCondition   추가 단위 (년/월/일/시/분/초)
     * @param durationCondition 추가 값
     * @return 추가된 시간
     */
    private LocalDateTime makePlusLocalDateTimeWithCondition(LocalDateTime standardDateTime, String unitOfCondition, int durationCondition) {
        return switch (unitOfCondition) {
            case "y" -> standardDateTime.plusYears(durationCondition);
            case "M" -> standardDateTime.plusMonths(durationCondition);
            case "d" -> standardDateTime.plusDays(durationCondition);
            case "h" -> standardDateTime.plusHours(durationCondition);
            case "m" -> standardDateTime.plusMinutes(durationCondition);
            case "s" -> standardDateTime.plusSeconds(durationCondition);
            default -> standardDateTime.plusMinutes(10);
        };
    }

    /**
     * Spread 총 개수 구하기
     * @return Spread 총 개수
     */
    @Transactional(readOnly = true)
    public int getAllCount() {
        return (int) spreadRepository.count();
    }
}
