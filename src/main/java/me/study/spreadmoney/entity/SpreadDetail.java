package me.study.spreadmoney.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.study.spreadmoney.entity.enumerated.SpreadDetailStatus;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 뿌리기 상세 객체
 */
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "SPREAD_DETAIL")
public class SpreadDetail {
    @Id @GeneratedValue
    @Column(name = "spread_detail_id")
    private Long id; //데이터 고유 아이디

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "spread_id", nullable = false, updatable = false)
    private Spread spread; //상위 뿌리기 객체

    @Column(nullable = false, updatable = false)
    private int distributedMoney; //개별 뿌려진 금액

    private int receivedUserId; //받아간 사용자 아이디

    private LocalDateTime receivedDateTime; //받아간 시간

    @Enumerated(EnumType.STRING)
    private SpreadDetailStatus status = SpreadDetailStatus.RECEIVABLE; //받을 수 있는지, 받았는지 상태

    /**
     * 뿌리기 상세 객체 생성 메서드
     * @param spread 상위 뿌리기 객체
     * @param distributedMoney 뿌려진 금액
     */
    public static SpreadDetail createSpreadDetails(Spread spread, int distributedMoney) {
        SpreadDetail spreadDetail = new SpreadDetail();
        spreadDetail.setSpread(spread);
        spreadDetail.setDistributedMoney(distributedMoney);
        spread.getSpreadDetails().add(spreadDetail);
        return spreadDetail;
    }

    /**
     * 금액 받아간 후 정보 수정 (받은 사람, 받은 시간, 상태)
     * @param userId 받은 사람
     */
    public void setReceiveInfo(int userId) {
        this.receivedUserId = userId;
        this.receivedDateTime = LocalDateTime.now();
        this.status = SpreadDetailStatus.DONE;
    }

    @Override
    public String toString() {
        return "SpreadDetails{" +
                "id=" + id +
                ", spread.token=" + spread.getToken() +
                ", distributedMoney=" + distributedMoney +
                ", receivedUserId=" + receivedUserId +
                ", receivedDateTime=" + receivedDateTime +
                ", status=" + status +
                '}';
    }
}
