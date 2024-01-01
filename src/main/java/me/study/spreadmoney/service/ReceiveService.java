package me.study.spreadmoney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.entity.SpreadDetail;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import me.study.spreadmoney.repository.SpreadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 받기 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveService {
    private final SpreadRepository spreadRepository;
    private final ExpiredSpreadService expiredSpreadService;

    /**
     * 받기
     * 1) 자신이 뿌리기한 건은 자신이 받을 수 없음
     * 3) 뿌리기가 호출된 대화방과 동일한 대화방만 받을 수 있음
     * 3) 만료된 뿌리기는 받을 수 없음
     * 4) 받을 금액이 남아있어야 가능
     * 5) 사용자 당 한번 받을 수 있음
     * @param userId 받기 요청 사용자 아이디
     * @param roomId 받기 요청 사용자 대화방 아이디
     * @param token 받기를 요청하는 뿌리기 token
     * @return 받은 금액
     */
    @Transactional
    public int receiveMoney(int userId, String roomId, String token) {
        //Token 검증, Spread 가져오기
        Optional<Spread> findSpread = spreadRepository.findByToken(token);
        if(findSpread.isEmpty())
            throw new PredictableRuntimeException("유효하지 않는 Token 값입니다.");
        Spread spread = findSpread.get();

        LocalDateTime now = LocalDateTime.now();
        //만료시간 검증
        if (spread.getViewableExpireDateTime().isBefore(now)){
            //조회 만료 데이터 이관 (Spread, SpreadDetail -> ExpiredSpread)
            //expiredSpreadService.spreadToExpireInApiRequest(spread.getId()); -> Scheduler only 수정 (2024.01.01)
            throw new PredictableRuntimeException("유효하지 않는 Token 값입니다.");
        } else if (spread.getReceivableExpireDateTime().isBefore(now)) {
            throw new PredictableRuntimeException("뿌린 건의 받기 시간이 만료 되었습니다. 만료 일시: "+spread.getReceivableExpireDateTime());
        }

        //뿌리기 계정 검증
        if (spread.getUserId() == userId)
            throw new PredictableRuntimeException("자신이 뿌리기한 건은 자신이 받을 수 없습니다.");

        //대화방 검증
        if (!spread.getRoomId().equals(roomId))
            throw new PredictableRuntimeException("뿌리기가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.");

        //남은금액 검증
        if(spread.getRemainMoney() <= 0)
            throw new PredictableRuntimeException("이미 모두 받아간 뿌리기입니다.");

        //사용자 중복 받기 검증
        List<SpreadDetail> spreadDetailList = spread.getSpreadDetails();
        long count = spreadDetailList.stream().filter(d -> d.getReceivedUserId() == userId).count();
        if (count != 0)
            throw new PredictableRuntimeException("이미 받은 뿌리기입니다.");

        //사용 전 SpreadDetail 가져오기
        Optional<SpreadDetail> firstDetail = spreadDetailList.stream()
                .filter(d -> d.getStatus().equals(SpreadDetailStatus.RECEIVABLE))
                .findFirst();
        if(firstDetail.isEmpty())
            throw new PredictableRuntimeException("뿌리기 받기에 실패했습니다. 다시 시도 해주세요. 계속해서 문제가 발생한다면 관리자에게 문의해주십시오.");

        //뿌리기
        SpreadDetail spreadDetail = firstDetail.get();
        int distributedMoney = spreadDetail.getDistributedMoney();
        spread.updateRemainInfo(distributedMoney);
        spreadDetail.setReceiveInfo(userId);

        return distributedMoney;
    }
}
