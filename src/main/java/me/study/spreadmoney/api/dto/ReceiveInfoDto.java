package me.study.spreadmoney.api.dto;

import lombok.Data;

@Data
public class ReceiveInfoDto {
    private int receivedMoney;
    private int receivedUserId;

    public ReceiveInfoDto(int receivedMoney, int receivedUserId) {
        this.receivedMoney = receivedMoney;
        this.receivedUserId = receivedUserId;
    }
}
