package com.travelock.server.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyBlockConnect extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyBlockConnectId;

    @Column(columnDefinition = "INT NOT NULL COMMENT '블록 순서(N번째)'")
    private Integer blockNum;

    // Daily Course 여러개와 Full Block 여러개가 존재
    // Daily Course, Full Block은 DailyBlockConnect 엔티티 조회시 즉시 사용 - FetchType.EAGER(default)
    // Daily Block Connect : Daily Course = N : 1
    @ManyToOne
    @JoinColumn(name = "daily_course_id", nullable = false)
    private DailyCourse dailyCourse;

    // Daily Block Connect : Full Block = N : 1
    @ManyToOne
    @JoinColumn(name = "full_block_id", nullable = false)
    private FullBlock fullBlock;

    public void newConnect(Integer blockNum, DailyCourse dailyCourse, FullBlock fullBlock){
        this.blockNum = blockNum;
        this.dailyCourse = dailyCourse;
        this.fullBlock = fullBlock;
    }
}
