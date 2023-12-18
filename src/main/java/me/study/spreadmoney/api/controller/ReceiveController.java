package me.study.spreadmoney.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.api.response.SuccessResult;
import me.study.spreadmoney.service.ReceiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static me.study.spreadmoney.api.ControllerHelper.*;

/**
 * 받기 API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReceiveController {
    private final ReceiveService receiveService;

    /**
     * 받기 API
     * token(요청값)에 해당하는 뿌리기 건 중 할당되지 않은 분배건 하나를 사용자에게 할당
     * @param userId Header) 받기 요청 사용자 ID
     * @param roomId Header) 받기 요청 대화방 ID
     * @param receiveReq Body) 받기 요청값 객체 (String Token - 뿌리기 토큰)
     * @return 받은 금액 값
     */
    @PostMapping("/api/receive")
    public ResponseEntity<Result> receiveRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid ReceiveReq receiveReq
    ) {
        log.info("받기 요청, userId: {}, roomId: {}, body: {}", userId, roomId, receiveReq.toString());

        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //비즈니스 로직
        int receivedMoney = receiveService.receiveMoney(userId, roomId, receiveReq.token);

        //응답 값 생성 및 설정
        ReceiveRespData receiveRespData = new ReceiveRespData();
        receiveRespData.setReceivedMoney(receivedMoney);

        log.info("받기 응답, userId: {}, roomId: {}, body: {}", userId, roomId, receiveRespData.toString());
        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, receiveRespData));
    }

    /**
     * 받기 요청 객체
     */
    @Data
    static class ReceiveReq {
        @NotBlank(message = "Token 값은 필수 값입니다.")
        @Size(min = 3, max = 3, message = "Token 값은 3자리 문자열입니다.")
        private String token; //받으려는 뿌리기 token 값
    }

    /**
     * 받기 응답 객체
     */
    @Data
    static class ReceiveRespData {
        private int receivedMoney; //받은 금액

        @Override
        public String toString() {
            return "{" +
                    "receivedMoney=" + receivedMoney +
                    '}';
        }
    }
}
