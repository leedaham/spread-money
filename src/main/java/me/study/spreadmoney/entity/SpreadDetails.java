package me.study.spreadmoney.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;

import java.time.LocalDateTime;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpreadDetails {
    @Id @GeneratedValue
    @Column(name = "spread_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spread_id", nullable = false, updatable = false)
    private Spread spread;

    @Column(nullable = false, updatable = false)
    private int distributedMoney;

    private int receivedUserId;

    private LocalDateTime distributeDateTime;

    @Enumerated(EnumType.STRING)
    private SpreadDetailStatus status = SpreadDetailStatus.RECEIVABLE;

    public static SpreadDetails createSpreadDetails(Spread spread, int distributedMoney) {
        SpreadDetails spreadDetails = new SpreadDetails();
        spreadDetails.setSpread(spread);
        spreadDetails.setDistributedMoney(distributedMoney);
        spread.getSpreadDetails().add(spreadDetails);
        return spreadDetails;
    }

    public void setReceiveInfo(int userId) {
        this.receivedUserId = userId;
        this.distributeDateTime = LocalDateTime.now();
        this.status = SpreadDetailStatus.DONE;
    }

    @Override
    public String toString() {
        return "SpreadDetails{" +
                "id=" + id +
                ", spread.token=" + spread.getToken() +
                ", distributedMoney=" + distributedMoney +
                ", receivedUserId=" + receivedUserId +
                ", distributeDateTime=" + distributeDateTime +
                ", status=" + status +
                '}';
    }
}
