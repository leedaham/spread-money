package me.study.spreadmoney.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.spreadmoney.api.dto.LookUpSpreadInfoDto;
import me.study.spreadmoney.api.dto.ReceiveInfoDto;
import me.study.spreadmoney.api.response.Result;
import me.study.spreadmoney.api.response.SuccessResult;
import me.study.spreadmoney.exception.CustomRuntimeException;
import me.study.spreadmoney.service.SpreadAndDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ApiController {
    private static final String SUCCESS_MSG = "요청이 성공적으로 처리되었습니다.";
    private static final String HEADER_USER_ID = "X-USER-ID";
    private static final String HEADER_ROOM_ID = "X-USER-ID";

    private final SpreadAndDetailsService spreadAndDetailsService;

    //로그 남겨야 해
    //각각 테스트 확인해야해

    /**
     * 뿌리기 API
     * 뿌릴 금액, 뿌릴 인원 (요청값)에 맞게 뿌리기 생성
     * @param userId 뿌리기 요청 사용자 ID (헤더)
     * @param roomId 뿌리기 요청 대화방 ID (헤더)
     * @param spreadReq 뿌리기 요청값 객체 (바디)
     * @return 뿌리기 token 값
     */
    @PostMapping("/spread")
    public ResponseEntity<Result> spreadRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid SpreadReq spreadReq
    ) {

        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //request 값 확인
        int totalAmountOfMoney = spreadReq.totalMoney;
        int totalPeopleNum = spreadReq.totalPeopleNum;
        isValidAmountAndPeopleNum(totalAmountOfMoney, totalPeopleNum);

        //비즈니스 로직
        String token = spreadAndDetailsService.spreadMoney(userId, roomId, totalAmountOfMoney, totalPeopleNum);

        //응답 값 생성 및 설정
        SpreadRespData spreadRespData = new SpreadRespData();
        spreadRespData.setToken(token);

        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, spreadRespData));
    }

    /**
     * 받기 API
     * token(요청값)에 해당하는 뿌리기 건 중 할당되지 않은 분배건 하나를 사용자에게 할당
     * 제약 1) 사용자 당 한번 받을 수 있음
     * 제약 2) 자신이 뿌리기한 건은 자신이 받을 수 없음
     * 제약 3) 뿌리기가 호출된 대화방과 동일한 대화방만 받을 수 있음
     * 제약 4) 뿌린 건은 10분간 유효
     * @param userId 받기 요청 사용자 ID (헤더)
     * @param roomId 받기 요청 대화방 ID (헤더)
     * @param receiveReq 받기 요청값 객체 (바디)
     * @return
     */
    @PostMapping("/receive")
    public ResponseEntity<Result> receiveRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid ReceiveReq receiveReq
    ) {

        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //비즈니스 로직
        int receivedMoney = spreadAndDetailsService.receiveMoney(userId, roomId, receiveReq.token);

        //응답 값 생성 및 설정
        ReceiveRespData receiveRespData = new ReceiveRespData();
        receiveRespData.setReceivedMoney(receivedMoney);

        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, receivedMoney));
    }

    @GetMapping("/lookup")
    public ResponseEntity<Result> lookUpRequest(
            @RequestHeader(HEADER_USER_ID) int userId,
            @RequestHeader(HEADER_ROOM_ID) String roomId,
            @RequestBody @Valid LookUpReq lookUpReq
    ) {
        //HTTP Header 값 확인 (X-USER-ID, X-ROOM-ID)
        checkHeaderValue(userId, roomId);

        //비즈니스 로직
        LookUpSpreadInfoDto lookUpSpreadInfoDto = spreadAndDetailsService.lookUpSpreadInfo(userId, lookUpReq.token);

        //응답 값 생성 및 설정
        LookUpRespData lookUpRespData = new LookUpRespData(lookUpSpreadInfoDto);

        //응답
        return ResponseEntity.ok(new SuccessResult(SUCCESS_MSG, lookUpRespData));
    }




    private void checkHeaderValue(int userId, String roomId) {
        if (userId >= 0)
            throw new CustomRuntimeException("Header의 X-USER-ID 값이 올바르지 않습니다.");
        else if(roomId == null || roomId.isEmpty())
            throw new CustomRuntimeException("Header의 X-ROOM-ID 값이 없거나 비어있습니다.");
    }

    private void isValidAmountAndPeopleNum(int totalAmountOfMoney, int totalPeopleNum) {
        if (totalPeopleNum <= totalAmountOfMoney)
            throw new CustomRuntimeException(String.format(
                    "뿌릴 금액이 뿌릴 인원보다 같거나 커야합니다. 뿌릴 금액: %d, 뿌릴 인원: %d",
                    totalAmountOfMoney, totalPeopleNum));
    }

    /**
     * 뿌리기 요청 요청값
     * totalMoney: 뿌릴 금액
     * totalPeopleNum: 뿌릴 인원
     */
    @Data
    static class SpreadReq {
        @NotNull(message = "뿌릴 금액은 필수 값입니다.")
        @Positive(message = "뿌릴 금액은 0 이상의 양수만 가능합니다.")
        private int totalMoney;

        @NotNull(message = "뿌릴 인원은 필수 값입니다.")
        @Positive(message = "뿌릴 인원은 0 이상의 양수만 가능합니다.")
        private int totalPeopleNum;
    }
    @Data
    static class SpreadRespData {
        private String token;
    }

    @Data
    static class ReceiveReq {
        @NotBlank(message = "Token 값은 필수 값입니다.")
        @Size(min = 3, max = 3, message = "Token 값은 3자리 문자열입니다.")
        private String token;
    }

    @Data
    static class ReceiveRespData {
        private int receivedMoney;
    }

    @Data
    static class LookUpReq {
        @NotBlank(message = "Token 값은 필수 값입니다.")
        @Size(min = 3, max = 3, message = "Token 값은 3자리 문자열입니다.")
        private String token;
    }

    @Data
    static class LookUpRespData {
        private LocalDateTime spreadDateTime;
        private int totalMoney;
        private int totalReceivedMoney;
        private List<ReceiveInfoDto> receiveInfoDtoList;

        public LookUpRespData(LookUpSpreadInfoDto lookUpSpreadInfoDto) {
            this.spreadDateTime = lookUpSpreadInfoDto.getSpreadDateTime();
            this.totalMoney = lookUpSpreadInfoDto.getTotalMoney();
            this.totalReceivedMoney = lookUpSpreadInfoDto.getTotalReceivedMoney();
            this.receiveInfoDtoList = lookUpSpreadInfoDto.getReceiveInfoDtoList();
        }
    }
}
