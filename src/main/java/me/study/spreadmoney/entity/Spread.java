package me.study.spreadmoney.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 뿌리기 객체, 하나의 뿌리기 객체는 최소 1개 이상의 뿌리기 상세 객체를 가짐.
 */
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "SPREAD")
public class Spread {
    @Id @GeneratedValue
    @Column(name = "spread_id")
    private Long id; //데이터 고유 아이디
    @Column(nullable = false, updatable = false, unique = true)
    private String token; //뿌리기 고유 token
    @Column(nullable = false, updatable = false)
    private int userId; //뿌리 사람 아이디
    @Column(nullable = false, updatable = false)
    private String roomId; //뿌린 대화방 아이디
    @Column(nullable = false, updatable = false)
    private int totalMoney; //뿌린 금액
    @Column(nullable = false, updatable = false)
    private int totalPeopleNum; //뿌린 인원
    @Column(nullable = false)
    private int remainMoney; //받아가지 않은 금액
    @Column(nullable = false)
    private int remainPeopleNum; //받아가지 않은 인원
    @Column(nullable = false, updatable = false)
    private LocalDateTime spreadDateTime; //뿌린 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime receivableExpireDateTime; //받기 만료 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime viewableExpireDateTime; //조회 만료 시각

    @OneToMany(mappedBy = "spread", cascade = CascadeType.ALL)
    private List<SpreadDetail> spreadDetails = new ArrayList<>(); //하위 뿌리기 상세 객체 리스트, 뿌린 금액 별 객체들

    /**
     * 뿌리기 객체 생성 메서드
     */
    public static Spread createSpread(
            String token, int userId, String roomId,
            int totalMoney, int totalPeopleNum, int remainMoney, int remainPeopleNum,
            LocalDateTime spreadDateTime, LocalDateTime receivableExpireDateTime, LocalDateTime viewableExpireDateTime
    ) {
        Spread spread = new Spread();
        spread.setToken(token);
        spread.setUserId(userId);
        spread.setRoomId(roomId);
        spread.setTotalMoney(totalMoney);
        spread.setTotalPeopleNum(totalPeopleNum);
        spread.setRemainMoney(totalMoney);
        spread.setRemainMoney(remainMoney);
        spread.setRemainPeopleNum(remainPeopleNum);
        spread.setSpreadDateTime(spreadDateTime);
        spread.setReceivableExpireDateTime(receivableExpireDateTime);
        spread.setViewableExpireDateTime(viewableExpireDateTime);
        return spread;
    }

    /**
     * 일부 금액 받아간 후 정보 수정 (남은 금액, 남은 인원)
     * @param takenMoney 받아간 금액
     */
    public void updateRemainInfo(int takenMoney) {
        this.remainMoney -= takenMoney;
        this.remainPeopleNum -= 1;
    }

    /**
     * 지금까지 받아간 금액 구하기
     * @return 지금까지 받아간 금액
     */
    public int getTotalReceivedMoney() {
        return (totalMoney - remainMoney);
    }

    @Override
    public String toString() {
        return "Spread{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", userId=" + userId +
                ", roomId='" + roomId + '\'' +
                ", totalMoney=" + totalMoney +
                ", totalPeopleNum=" + totalPeopleNum +
                ", remainMoney=" + remainMoney +
                ", remainPeopleNum=" + remainPeopleNum +
                ", spreadDateTime=" + spreadDateTime +
                ", receivableExpireDateTime=" + receivableExpireDateTime +
                ", viewableExpireDateTime=" + viewableExpireDateTime +
                ", spreadDetails=" + spreadDetails +
                '}';
    }
}
