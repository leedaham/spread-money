package me.study.spreadmoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.dto.LookUpSpreadInfoDto;
import me.study.spreadmoney.api.dto.ReceiveInfoDto;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import me.study.spreadmoney.repository.SpreadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LookUpService {

    private final SpreadRepository spreadRepository;
    private final ExpiredSpreadService expiredSpreadService;

    /**
     * 조회
     * token 에 해당하는 뿌리기 건의 현재 상태 반환
     * 1) 조회 만료일이 지나면 조회할 수 없음
     * 2) 뿌린 사람만 조회를 할 수 있음
     * 현재 상태: 뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보([받은 금액], [받은 사용자 아이디] 리스트)
     * @param userId 조회 요청 사용자 아이디
     * @param token 조회 요청 token
     * @return token 에 해당하는 뿌리기 현재 상태
     */
    @Transactional(readOnly = true)
    public LookUpSpreadInfoDto lookUpSpreadInfo(int userId, String token) {
        //Token 검증, Spread 가져오기
        Optional<Spread> findSpread = spreadRepository.findByToken(token);
        if(findSpread.isEmpty())
            throw new PredictableRuntimeException("유효하지 않는 Token 값입니다.");
        Spread spread = findSpread.get();

        //조회 가능 사용자 검증
        if(spread.getUserId() != userId)
            throw new PredictableRuntimeException("뿌린 사람 자신만 조회를 할 수 있습니다.");

        //조회 가능 기간 검증
        if(spread.getViewableExpireDateTime().isBefore(LocalDateTime.now())){
            //조회 만료 데이터 이관 (Spread, SpreadDetail -> ExpiredSpread)
            //expiredSpreadService.spreadToExpireInApiRequest(spread.getId()); -> Scheduler only 수정 (2024.01.01)
            throw new PredictableRuntimeException("조회 가능일이 지났습니다.");
        }

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
