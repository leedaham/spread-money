package me.study.spreadmoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.entity.ExpiredSpread;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
import me.study.spreadmoney.exception.UnrelatedWithClientRequestException;
import me.study.spreadmoney.repository.ExpiredSpreadRepository;
import me.study.spreadmoney.repository.SpreadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiredSpreadService {
    private final ExpiredSpreadRepository expiredSpreadRepository;
    private final SpreadRepository spreadRepository;

    /**
     * 만료된 객체를 Spread, SpreadDetail 테이블에서 ExpireSpread 테이블로 옮김
     * @param spreadId 만료된 Spread ID
     */
    @Transactional
    public void spreadToExpire(Long spreadId) {
        //Token 검증, Spread 가져오기
        Optional<Spread> findSpread = spreadRepository.findById(spreadId);
        if(findSpread.isEmpty())
            throw new UnrelatedWithClientRequestException("유효하지 않는 Spread Id 값입니다.");
        Spread spread = findSpread.get();

        //Spread Detail 정보 가져오기
        List<SpreadDetail> spreadDetailList = spread.getSpreadDetails();

        //Spread Detail 정보 중 저장할 정보만 String 으로 가공
        String spreadDetailsInfo = makeSpreadDetailsInfo(spreadDetailList);

        //ExpireSpread 객체 생성
        ExpiredSpread expireSpread = ExpiredSpread.createExpireSpread(
                spread.getToken(),
                spread.getUserId(),
                spread.getRoomId(),
                spread.getTotalMoney(),
                spread.getTotalPeopleNum(),
                spread.getRemainMoney(),
                spread.getRemainPeopleNum(),
                spread.getSpreadDateTime(),
                spreadDetailsInfo
        );

        //ExpireSpread 저장
        expiredSpreadRepository.save(expireSpread);

        //Spread 삭제, SpreadDetail 삭제(영속성 전이)
        spreadRepository.delete(spread);

    }

    /**
     * 뿌리기 상세 객체 정보들을 문자열로 변환
     * @param spreadDetailList 뿌리기 상세 객체 정보 리스트
     * @return 문자열로 변환된 뿌리기 상세 객체 정보들
     */
    private String makeSpreadDetailsInfo(List<SpreadDetail> spreadDetailList) {
        List<String> spreadDetailStrList =
                spreadDetailList.stream()
                .map(d -> {
                    String info;
                    int money = d.getDistributedMoney();
                    SpreadDetailStatus status = d.getStatus();
                    if (status.equals(SpreadDetailStatus.RECEIVABLE)) {
                        info = format("금액: %d (받아가지 않음)", money);
                    } else {
                        int receivedUserId = d.getReceivedUserId();
                        LocalDateTime receivedDateTime = d.getReceivedDateTime();
                        info = format("금액: %d (id: %d, date: %s)", money, receivedUserId, receivedDateTime);
                    }
                    return info;
                })
                .toList();

        return String.join(",", spreadDetailStrList);
    }


    /**
     * 조회 기간이 만료된 Spread 찾기
     * @return 조회 기간이 만료된 Spread List
     */
    @Transactional(readOnly = true)
    public List<Spread> findExpiredSpread() {
        return spreadRepository.findByViewableExpireDateTimeBefore(LocalDateTime.now());
    }

    /**
     * API 요청 중 만료 Spread 데이터 이관은 사용자의 요청과 관련이 없음 여기서 발생하는 에러는 사용자에게 전달하지 않음.
     * @param spreadId 만료된 Spread ID
     */
    @Transactional
    public void spreadToExpireInApiRequest(Long spreadId) {
        try {
            log.info("Spread, SpreadDetail -> ExpiredSpread 데이터 이관, spreadId: {}", spreadId);
            spreadToExpire(spreadId);
        } catch (UnrelatedWithClientRequestException ex) {
            log.warn("Spread, SpreadDetail -> ExpiredSpread 데이터 이관 중 오류 발생");
            log.error("[{}] {}", spreadId, ex.getMessage());
        }
    }
}
