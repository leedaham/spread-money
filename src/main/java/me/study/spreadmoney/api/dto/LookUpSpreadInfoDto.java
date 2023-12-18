package me.study.spreadmoney.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 조회시 응답하는 현재 상태 DTO
 */
@Data
public class LookUpSpreadInfoDto {
    private LocalDateTime spreadDateTime; //뿌린 시각
    private int totalMoney; //뿌린 금액
    private int totalReceivedMoney; //받기 완료된 금액
    private List<ReceiveInfoDto> receiveInfoDtoList; //받기 완료된 정보([받은 금액],[받은 사용자 아이디] 리스트)

    public LookUpSpreadInfoDto(LocalDateTime spreadDateTime, int totalMoney, int totalReceivedMoney, List<ReceiveInfoDto> receiveInfoDtoList) {
        this.spreadDateTime = spreadDateTime;
        this.totalMoney = totalMoney;
        this.totalReceivedMoney = totalReceivedMoney;
        this.receiveInfoDtoList = receiveInfoDtoList;
    }
}
