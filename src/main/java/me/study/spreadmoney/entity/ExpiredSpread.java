package me.study.spreadmoney.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpiredSpread {
    @Id @GeneratedValue
    @Column(name = "expire_spread_id")
    private Long id;
    @Column(nullable = false, updatable = false)
    private String token;
    @Column(nullable = false, updatable = false)
    private int spreadUserId;
    @Column(nullable = false, updatable = false)
    private String spreadRoomId;
    @Column(nullable = false, updatable = false)
    private int totalAmount;
    @Column(nullable = false, updatable = false)
    private int totalPeopleNum;
    @Column(nullable = false, updatable = false)
    private int remainAmount;
    @Column(nullable = false, updatable = false)
    private int remainPeopleNum;
    @Column(nullable = false, updatable = false)
    private LocalDateTime spreadDateTime;
    @Column(nullable = false, updatable = false)
    private String spreadDetails;
}
