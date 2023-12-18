package me.study.spreadmoney.api.dto;

import lombok.Data;

/**
 * 받은 사람과 받은 금액 객체
 */
@Data
public class ReceiveInfoDto {
    private int receivedMoney; //받은 금액
    private int receivedUserId; //받은 사용자 아이디

    public ReceiveInfoDto(int receivedMoney, int receivedUserId) {
        this.receivedMoney = receivedMoney;
        this.receivedUserId = receivedUserId;
    }
}
