package me.study.spreadmoney.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.api.response.SuccessResult;
import me.study.spreadmoney.exception.PredictableRuntimeException;
import me.study.spreadmoney.service.SpreadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;
import static me.study.spreadmoney.api.ControllerHelper.*;

/**
 * 뿌리기 API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SpreadController {
    private final SpreadService spreadService;

    /**
     * 뿌리기 API
     * 뿌릴 금액, 뿌릴 인원 (요청값)에 맞게 뿌리기 생성 및 고유 token 발급
     * @param userId Header) 뿌리기 요청 사용자 ID
     * @param roomId Header) 뿌리기 요청 대화방 ID
     * @param spreadReq Body) 뿌리기 요청값 객체 (int totalMoney - 뿌릴 금액, int totalPeopleNum - 뿌릴 인원)
     * @return 생성된 뿌리기 token 값
     */
    @PostMapping("/api/spread")
    public ResponseEntity<Result> spreadRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid SpreadReq spreadReq
    ) {
        log.info("뿌리기 요청, userId: {}, roomId: {}, body: {}", userId, roomId, spreadReq.toString());

        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //request 값 확인
        int totalAmountOfMoney = spreadReq.totalMoney;
        int totalPeopleNum = spreadReq.totalPeopleNum;
        isValidAmountAndPeopleNum(totalAmountOfMoney, totalPeopleNum);

        //비즈니스 로직
        String token = spreadService.spreadMoney(userId, roomId, totalAmountOfMoney, totalPeopleNum);

        //응답 값 생성 및 설정
        SpreadRespData spreadRespData = new SpreadRespData();
        spreadRespData.setToken(token);

        log.info("뿌리기 응답, userId: {}, roomId: {}, body: {}", userId, roomId, spreadRespData.toString());
        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, spreadRespData));
    }

    /**
     * 뿌리기 API 요청 값(뿌릴 금액, 뿌릴 인원) 검증
     * @param totalMoney 뿌릴 금액
     * @param totalPeopleNum 뿌릴 인원
     */
    private void isValidAmountAndPeopleNum(int totalMoney, int totalPeopleNum) {
        if (totalPeopleNum > totalMoney)
            throw new PredictableRuntimeException(format(
                    "뿌릴 금액이 뿌릴 인원보다 같거나 커야합니다. 뿌릴 금액: %d, 뿌릴 인원: %d",
                    totalMoney, totalPeopleNum));
    }

    /**
     * 뿌리기 요청 객체
     */
    @Data
    static class SpreadReq {
        @NotNull(message = "뿌릴 금액은 필수 값입니다.")
        @Positive(message = "뿌릴 금액은 0 이상의 양수만 가능합니다.")
        private int totalMoney; //뿌릴 금액

        @NotNull(message = "뿌릴 인원은 필수 값입니다.")
        @Positive(message = "뿌릴 인원은 0 이상의 양수만 가능합니다.")
        private int totalPeopleNum; //뿌릴 인원
    }

    /**
     * 뿌리기 응답 객체
     */
    @Data
    static class SpreadRespData {
        private String token; //생성된 뿌리기의 token 값

        @Override
        public String toString() {
            return "{" +
                    "token='" + token + '\'' +
                    '}';
        }
    }

}
