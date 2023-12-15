package me.study.spreadmoney.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LookUpSpreadInfoDto {
    private LocalDateTime spreadDateTime;
    private int totalMoney;
    private int totalReceivedMoney;
    private List<ReceiveInfoDto> receiveInfoDtoList;

    public LookUpSpreadInfoDto(LocalDateTime spreadDateTime, int totalMoney, int totalReceivedMoney, List<ReceiveInfoDto> receiveInfoDtoList) {
        this.spreadDateTime = spreadDateTime;
        this.totalMoney = totalMoney;
        this.totalReceivedMoney = totalReceivedMoney;
        this.receiveInfoDtoList = receiveInfoDtoList;
    }
}
