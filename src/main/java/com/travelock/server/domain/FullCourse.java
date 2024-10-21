package com.travelock.server.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FullCourse extends BaseTime implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fullCourseId;

    @Column(columnDefinition = "VARCHAR(100) NULL COMMENT '여행 타이틀'")
    private String title;

    @Column(columnDefinition="INT NOT NULL DEFAULT 0 COMMENT '좋아요 수'")
    private Integer favoriteCount;

    @Column(columnDefinition = "INT NOT NULL DEFAULT 0 COMMENT '스크랩 수'")
    private Integer scarpCount;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '활성화 상태'")
    private String activeStatus;

    // Full Course : Connect entity = N : 1
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;

    // Full Course : Connect entity = 1 : N
    @OneToMany(mappedBy = "fullCourse", fetch = FetchType.LAZY)
    private List<FullAndDailyCourseConnect> fullAndDailyCourseConnects;

    @OneToMany(mappedBy = "fullCourse", fetch = FetchType.LAZY)
    private List<FullCourseFavorite> fullCourseFavorites;

    @OneToMany(mappedBy = "fullCourse", fetch = FetchType.LAZY)
    private List<FullCourseScrap> fullCourseScraps;


    /**
     * FullCourse Set Function
     */
    // 전체일정 저장
    public void addFullCourse(String title, Member member) {
        this.title = title;

        // 좋아요, 스크랩 수 기본값
        this.favoriteCount = 0;
        this.scarpCount = 0;

        // 최초 생성시 활성화 상태는 Y
        this.activeStatus = "y";

        // Member, DailyCourses
        this.member = member;
    }

    public void changeTitle(String title){
        this.title = title;
    }


}
