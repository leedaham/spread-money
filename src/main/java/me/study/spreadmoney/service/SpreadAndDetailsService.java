package me.study.spreadmoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.dto.LookUpSpreadInfoDto;
import me.study.spreadmoney.api.dto.ReceiveInfoDto;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetails;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
import me.study.spreadmoney.exception.CustomRuntimeException;
import me.study.spreadmoney.repository.SpreadDetailsRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpreadAndDetailsService {

    @Value("${config.policy.spread-expire-date.unit}")
    private String spread_expire_date_unit;
    @Value("${config.policy.spread-expire-date.duration}")
    private int spread_expire_date_duration;
    @Value("${config.policy.viewable-date.unit}")
    private String viewable_date_unit;
    @Value("${config.policy.viewable-date.duration}")
    private int viewable_date_duration;

    private final SpreadRepository spreadRepository;
    private final SpreadDetailsRepository spreadDetailsRepository;
    private final Random random = new Random();

    @Transactional
    public String spreadMoney(int userId, String roomId, int totalMoney, int totalPeopleNum) {
        LocalDateTime spreadDateTime = LocalDateTime.now();
        LocalDateTime expireDateTime = makeExpireDateTime(spreadDateTime);
        LocalDateTime viewableDateTime = makeViewableDateTime(spreadDateTime);

        String token = makeToken();
        if (token == null)
            throw new CustomRuntimeException("뿌리기 요청 Token 생성에 실패했습니다. 다시 시도 해주세요. 계속해서 문제가 발생한다면 관리자에게 문의해주십시오.");


        int remainMoney = totalMoney;
        int remainPeopleNum = totalPeopleNum;
        Spread spread = Spread.createSpread(
                token, userId, roomId,
                totalMoney, totalPeopleNum, remainMoney, remainPeopleNum,
                spreadDateTime, expireDateTime, viewableDateTime);
        spreadRepository.save(spread);

        List<Integer> distributedMoneyList = makeRandomSpreadMoney(totalMoney, totalPeopleNum);
        for (Integer distributedMoney : distributedMoneyList) {
            SpreadDetails spreadDetails = SpreadDetails.createSpreadDetails(spread, distributedMoney);
            spreadDetailsRepository.save(spreadDetails);
        }

        return token;
    }


    @Transactional
    public String makeToken() {
        String token = null;

        int tryNum = 0;
        while (tryNum < 1000) {
            //토큰 생성
            token = makeTokenStr();

            //토큰 Unique 확인
            boolean uniqueToken = isUniqueToken(token);
            if (uniqueToken) break;
            else tryNum++;
        }

        return token;
    }

    private String makeTokenStr() {
        String token;
        String candidateChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int index = random.nextInt(candidateChars.length());
            sb.append(candidateChars.charAt(index));
        }
        token = sb.toString();
        return token;
    }

    private boolean isUniqueToken(String token) {
        int count = spreadRepository.countByToken(token);
        return (count == 0);
    }

    private List<Integer> makeRandomSpreadMoney(int totalMoney, int totalPeopleNum) {
        List<Integer> list = new ArrayList<>();

        int remaining = totalMoney;
        for (int i = 1; i < totalPeopleNum; i++) {
            int randomMaxValue = remaining - (totalPeopleNum - i);
            int randomNumber = random.nextInt(randomMaxValue-1) + 1;
            list.add(randomNumber);
            remaining -= randomNumber;
        }
        list.add(remaining);

        return list;
    }

    private LocalDateTime makeExpireDateTime(LocalDateTime spreadDateTime) {
        return switch (spread_expire_date_unit) {
            case "y" -> spreadDateTime.plusYears(spread_expire_date_duration);
            case "M" -> spreadDateTime.plusMonths(spread_expire_date_duration);
            case "d" -> spreadDateTime.plusDays(spread_expire_date_duration);
            case "h" -> spreadDateTime.plusHours(spread_expire_date_duration);
            case "m" -> spreadDateTime.plusMinutes(spread_expire_date_duration);
            case "s" -> spreadDateTime.plusSeconds(spread_expire_date_duration);
            default -> spreadDateTime.plusMinutes(10);
        };
    }
    private LocalDateTime makeViewableDateTime(LocalDateTime spreadDateTime) {
        return switch (viewable_date_unit) {
            case "y" -> spreadDateTime.plusYears(viewable_date_duration);
            case "M" -> spreadDateTime.plusMonths(viewable_date_duration);
            case "d" -> spreadDateTime.plusDays(viewable_date_duration);
            case "h" -> spreadDateTime.plusHours(viewable_date_duration);
            case "m" -> spreadDateTime.plusMinutes(viewable_date_duration);
            case "s" -> spreadDateTime.plusSeconds(viewable_date_duration);
            default -> spreadDateTime.plusMinutes(10);
        };
    }

    //검증단계와 받기단계 나누기
    @Transactional
    public int receiveMoney(int userId, String roomId, String token) {
        //Token 검증, Spread 가져오기
        Optional<Spread> findSpread = spreadRepository.findByToken(token);
        if(findSpread.isEmpty())
            throw new CustomRuntimeException("유효하지 않는 Token 값입니다.");
        Spread spread = findSpread.get();

        //뿌리기 계정 검증
        if (spread.getUserId() == userId)
            throw new CustomRuntimeException("자신이 뿌리기한 건은 자신이 받을 수 없습니다.");

        //대화방 검증
        if (!spread.getRoomId().equals(roomId))
            throw new CustomRuntimeException("뿌리기가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.");

        //만료시간 검증
        if (spread.getExpireDateTime().isBefore(LocalDateTime.now()))
            throw new CustomRuntimeException("뿌린 건이 만료 되었습니다. 만료 일시: "+spread.getExpireDateTime());

        //남은금액 검증
        if(spread.getRemainMoney() <= 0)
            throw new CustomRuntimeException("이미 모두 받아간 뿌리기입니다.");

        //사용자 중복 받기 검증
        List<SpreadDetails> spreadDetailsList = spread.getSpreadDetails();
        long count = spreadDetailsList.stream().filter(d -> d.getReceivedUserId() == userId).count();
        if (count != 0)
            throw new CustomRuntimeException("이미 받은 뿌리기입니다.");

        //사용 전 SpreadDetail 가져오기
        Optional<SpreadDetails> firstDetail = spreadDetailsList.stream()
                .filter(d -> d.getStatus().equals(SpreadDetailStatus.RECEIVABLE))
                .findFirst();
        if(firstDetail.isEmpty())
            throw new CustomRuntimeException("뿌리기 받기에 실패했습니다. 다시 시도 해주세요. 계속해서 문제가 발생한다면 관리자에게 문의해주십시오.");

        //뿌리기
        SpreadDetails spreadDetails = firstDetail.get();
        int distributedMoney = spreadDetails.getDistributedMoney();
        spread.updateRemainInfo(distributedMoney);
        spreadDetails.setReceiveInfo(userId);

        return distributedMoney;
    }

    @Transactional
    public LookUpSpreadInfoDto lookUpSpreadInfo(int userId, String token) {
        //Token 검증, Spread 가져오기
        Optional<Spread> findSpread = spreadRepository.findByToken(token);
        if(findSpread.isEmpty())
            throw new CustomRuntimeException("유효하지 않는 Token 값입니다.");
        Spread spread = findSpread.get();

        //조회 가능 사용자 검증
        if(spread.getUserId() != userId)
            throw new CustomRuntimeException("뿌린 사람 자신만 조회를 할 수 있습니다.");

        //조회 가능 시간 검증
        if(spread.getViewableDateTime().isBefore(LocalDateTime.now()))
            throw new CustomRuntimeException("조회 가능일이 지났습니다.");

        //받기 완료된 정보 가져오기
        List<ReceiveInfoDto> receiveInfoDtoList = spread.getSpreadDetails()
                .stream()
                .filter(d -> d.getStatus().equals(SpreadDetailStatus.DONE))
                .map(d -> new ReceiveInfoDto(d.getDistributedMoney(), d.getReceivedUserId()))
                .toList();


        //응답
        return new LookUpSpreadInfoDto(
                spread.getSpreadDateTime(),
                spread.getTotalMoney(),
                spread.getTotalReceivedMoney(),
                receiveInfoDtoList);
    }
}
