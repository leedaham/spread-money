package me.study.spreadmoney.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 만료된 뿌리기 객체, 조회 가능일이 지난 뿌리기 객체
 */
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "EXPIRED_SPREAD")
public class ExpiredSpread {
    @Id @GeneratedValue
    @Column(name = "expire_spread_id")
    private Long id; //데이터 고유 아이디
    @Column(nullable = false, updatable = false)
    private String token; //뿌리기 고유 token
    @Column(nullable = false, updatable = false)
    private int spreadUserId; //뿌린 사용자 아이디
    @Column(nullable = false, updatable = false)
    private String spreadRoomId; //뿌린 대화방 아이디
    @Column(nullable = false, updatable = false)
    private int totalMoney; //뿌린 금액
    @Column(nullable = false, updatable = false)
    private int totalPeopleNum; //뿌린 인원
    @Column(nullable = false, updatable = false)
    private int remainMoney; //남은 금액
    @Column(nullable = false, updatable = false)
    private int remainPeopleNum; //남은 인원
    @Column(nullable = false, updatable = false)
    private LocalDateTime spreadDateTime; //뿌린 시각
    @Column(nullable = false, updatable = false)
    private String spreadDetailsInfo; //뿌리기 상세 객체 정보

    /**
     * 만료 뿌리기 객체 생성 메서드
     */
    public static ExpiredSpread createExpireSpread(
            String token, int spreadUserId, String spreadRoomId,
            int totalMoney, int totalPeopleNum, int remainMoney, int remainPeopleNum,
            LocalDateTime spreadDateTime, String spreadDetailsInfo) {
        ExpiredSpread expiredSpread = new ExpiredSpread();
        expiredSpread.setToken(token);
        expiredSpread.setSpreadUserId(spreadUserId);
        expiredSpread.setSpreadRoomId(spreadRoomId);
        expiredSpread.setTotalMoney(totalMoney);
        expiredSpread.setTotalPeopleNum(totalPeopleNum);
        expiredSpread.setRemainMoney(remainMoney);
        expiredSpread.setRemainPeopleNum(remainPeopleNum);
        expiredSpread.setSpreadDateTime(spreadDateTime);
        expiredSpread.setSpreadDetailsInfo(spreadDetailsInfo);
        return expiredSpread;
    }

    @Override
    public String toString() {
        return "ExpiredSpread{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", spreadUserId=" + spreadUserId +
                ", spreadRoomId='" + spreadRoomId + '\'' +
                ", totalMoney=" + totalMoney +
                ", totalPeopleNum=" + totalPeopleNum +
                ", remainMoney=" + remainMoney +
                ", remainPeopleNum=" + remainPeopleNum +
                ", spreadDateTime=" + spreadDateTime +
                ", spreadDetailsInfo='" + spreadDetailsInfo + '\'' +
                '}';
    }
}
