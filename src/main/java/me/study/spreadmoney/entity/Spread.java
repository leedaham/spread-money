package me.study.spreadmoney.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spread {
    @Id @GeneratedValue
    @Column(name = "spread_id")
    private Long id;
    @Column(nullable = false, updatable = false, unique = true)
    private String token;
    @Column(nullable = false, updatable = false)
    private int userId;
    @Column(nullable = false, updatable = false)
    private String roomId;
    @Column(nullable = false, updatable = false)
    private int totalMoney;
    @Column(nullable = false, updatable = false)
    private int totalPeopleNum;
    @Column(nullable = false)
    private int remainMoney;
    @Column(nullable = false)
    private int remainPeopleNum;
    @Column(nullable = false, updatable = false)
    private LocalDateTime spreadDateTime;
    @Column(nullable = false, updatable = false)
    private LocalDateTime expireDateTime;
    @Column(nullable = false, updatable = false)
    private LocalDateTime viewableDateTime;

    @OneToMany(mappedBy = "spread", cascade = CascadeType.ALL)
    private List<SpreadDetails> spreadDetails = new ArrayList<>();

    public static Spread createSpread(String token, int userId, String roomId, int totalMoney, int totalPeopleNum, int remainMoney, int remainPeopleNum, LocalDateTime spreadDateTime, LocalDateTime expireDateTime, LocalDateTime viewableDateTime) {
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
        spread.setExpireDateTime(expireDateTime);
        spread.setViewableDateTime(viewableDateTime);
        return spread;
    }
    public void updateRemainInfo(int takenMoney) {
        this.remainMoney -= takenMoney;
        this.remainPeopleNum -= 1;
    }

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
                ", expireDateTime=" + expireDateTime +
                ", viewableDateTime=" + viewableDateTime +
                ", spreadDetails=" + spreadDetails +
                '}';
    }
}
