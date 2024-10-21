package com.travelock.server.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DailyCourse extends BaseTime implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyCourseId;

    @Column(columnDefinition="INT NOT NULL DEFAULT 0 COMMENT '좋아요 수'")
    private Integer favoriteCount;

    @Column(columnDefinition = "INT NOT NULL DEFAULT 0 COMMENT '스크랩 수'")
    private Integer scarpCount;

    // Daily Course : Member = N : 1
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Daily Course : Connect entity = 1 : N
    @OneToMany(mappedBy = "dailyCourse", fetch = FetchType.LAZY)
    private List<FullAndDailyCourseConnect> fullAndDailyCourseConnects;

    // Daily Course : Daily Block Connect = 1 : N
    @OneToMany(mappedBy = "dailyCourse", fetch = FetchType.LAZY)
    private List<DailyBlockConnect> dailyBlockConnects;

    @OneToMany(mappedBy = "dailyCourse", fetch = FetchType.LAZY)
    private List<DailyCourseFavorite> dailyCourseFavorites;

    @OneToMany(mappedBy = "dailyCourse", fetch = FetchType.LAZY)
    private List<DailyCourseScrap> dailyCourseScraps;

    /**
     * DailyCourse Set Function
     */
    public void addDailyCourse(Member member) {
        this.member = member;

        // 좋아요, 스크랩 수 기본값
        this.favoriteCount = 0;
        this.scarpCount = 0;
    }
}
