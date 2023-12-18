package me.study.spreadmoney.api.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.dto.LookUpSpreadInfoDto;
import me.study.spreadmoney.api.dto.ReceiveInfoDto;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.api.response.SuccessResult;
import me.study.spreadmoney.service.LookUpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static me.study.spreadmoney.api.ControllerHelper.*;

/**
 * 조회 API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LookUpController {
    private final LookUpService lookUpService;


    /**
     * 조회 API
     * token(요청값)에 해당하는 뿌리기 건의 현재 상태를 조회
     * @param userId Header) 조회 요청 사용자 ID
     * @param roomId Header) 조회 요청 대화방 ID
     * @param lookUpReq Body) 조회 요청값 객체 (String Token - 뿌리기 토큰)
     * @return 뿌린 시각, 뿌린 금액, 받기 완료된 금액, 받기 완료된 정보 ([받은 금액], [받은 사용자 아이디] 리스트)
     */
    @GetMapping("/api/lookup")
    public ResponseEntity<Result> lookUpRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid LookUpReq lookUpReq
    ) {
        log.info("조회 요청, userId: {}, roomId: {}, body: {}", userId, roomId, lookUpReq.toString());

        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //비즈니스 로직
        LookUpSpreadInfoDto lookUpSpreadInfoDto = lookUpService.lookUpSpreadInfo(userId, lookUpReq.token);

        //응답 값 생성 및 설정
        LookUpRespData lookUpRespData = new LookUpRespData(lookUpSpreadInfoDto);

        log.info("조회 응답, userId: {}, roomId: {}, body: {}", userId, roomId, lookUpRespData.toString());
        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, lookUpRespData));
    }

    /**
     * 조회 요청 객체
     */
    @Data
    static class LookUpReq {
        @NotBlank(message = "Token 값은 필수 값입니다.")
        @Size(min = 3, max = 3, message = "Token 값은 3자리 문자열입니다.")
        private String token; //조회하려는 뿌리기 token 값
    }

    /**
     * 조회 응답 객체
     */
    @Data
    static class LookUpRespData {
        private LocalDateTime spreadDateTime; //뿌린 시각
        private int totalMoney; //뿌린 금액
        private int totalReceivedMoney; //받기 완료된 금액
        private List<ReceiveInfoDto> receiveDetailInfo; //받기 완료된 정보([받은 금액], [받은 사용자 아이디])

        public LookUpRespData(LookUpSpreadInfoDto lookUpSpreadInfoDto) {
            this.spreadDateTime = lookUpSpreadInfoDto.getSpreadDateTime();
            this.totalMoney = lookUpSpreadInfoDto.getTotalMoney();
            this.totalReceivedMoney = lookUpSpreadInfoDto.getTotalReceivedMoney();
            this.receiveDetailInfo = lookUpSpreadInfoDto.getReceiveInfoDtoList();
        }

        @Override
        public String toString() {
            return "{" +
                    "spreadDateTime=" + spreadDateTime +
                    ", totalMoney=" + totalMoney +
                    ", totalReceivedMoney=" + totalReceivedMoney +
                    ", receiveDetailInfo=" + receiveDetailInfo +
                    '}';
        }
    }
}
