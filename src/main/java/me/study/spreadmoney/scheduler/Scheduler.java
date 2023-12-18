package me.study.spreadmoney.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.entity.Spread;
import me.study.spreadmoney.exception.UnrelatedWithClientRequestException;
import me.study.spreadmoney.service.ExpiredSpreadService;
import me.study.spreadmoney.service.SpreadService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Scheduler {

    private final ExpiredSpreadService expiredSpreadService;
    private final SpreadService spreadService;

    /**
     * 조회 만료가 된 Spread, SpreadDetail 데이터를 ExpiredSpread 로 옮기기
     */
    @Scheduled(cron = "${config.scheduler.spread-to-expire.cron}")
    public void spreadToExpireSpread() {
        log.info("만료된 뿌리기 확인 및 이관 - 시작");
        //Spread 에서 조회 만료된 데이터 검색
        List<Spread> expiredSpread = expiredSpreadService.findExpiredSpread();
        if (expiredSpread.isEmpty()) {
            log.info("만료 처리 할 뿌리기가 없습니다.");
            return;
        }
        //총 데이터 수 구하기
        int spreadAllCount = spreadService.getAllCount();
        log.info("total: {}, expired data: {}", spreadAllCount, expiredSpread.size());

        //Spread, SpreadDetail -> ExpiredSpread
        int count = 1;
        List<Long> errorIds = new ArrayList<>();
        for (Spread spread : expiredSpread) {
            Long id = spread.getId();
            try {
                expiredSpreadService.spreadToExpire(id);
                log.info("[{}] {}", count, id);
            } catch (UnrelatedWithClientRequestException ex) {
                errorIds.add(id);
                log.error("[{}] {} {}", count, id, ex.getMessage());
            }
            count++;
        }

        //마무리 로그
        log.info("총 실행 건: {}", count);
        if (errorIds.isEmpty()) {
            log.warn("오류 발생 뿌리기: {}", errorIds);
        } else {
            log.info("오류 발생 뿌리기가 없습니다.");
        }
        log.info("만료된 뿌리기 확인 및 이관 - 완료");
    }
}
