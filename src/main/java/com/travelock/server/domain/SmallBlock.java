package com.travelock.server.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mariadb.jdbc.message.server.ColumnDefinitionPacket;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmallBlock extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smallBlockId;

    @Column(columnDefinition = "VARCHAR(50) COMMENT 'API에서 받은 고유값'")
    private String placeId;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '경도값'")
    private String mapX;
    @Column(columnDefinition = "VARCHAR(100) COMMENT '위도값'")
    private String mapY;

    @Column(columnDefinition = "INT COMMENT '일정에 추가된(참조된) 수'")
    private Integer referenceCount;

    // Small Block : Middle Block = N : 1
    // 이 말은 즉, 여러개의 스몰블록이 하나의 미들블록을 참조할 수 있다는 것.
    @ManyToOne
    @JoinColumn(name = "middle_block_id")
    @JsonBackReference
    private MiddleBlock middleBlock;

    @Column(columnDefinition = "VARCHAR(29) COMMENT '장소 이름'")
    private String placeName;

    @OneToMany(mappedBy = "smallBlock")
    private List<SmallBlockReview> smallBlockReviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "big_block_id")
    @JsonBackReference
    private BigBlock bigBlock;  // State 테이블 참조


    // 레퍼 카운트 1씩 증가
    public void incrementReferenceCount() {
        this.referenceCount++;
    }

    public void createNewSmallBlock(String mapX, String mapY, String placeId, String placeName, MiddleBlock middleBlock, BigBlock bigBlock) {
        this.mapX = mapX;
        this.mapY = mapY;
        this.placeId = placeId;
        this.placeName = placeName;
        this.middleBlock = middleBlock;
        this.bigBlock = bigBlock;
        this.referenceCount = 0;
    }
}
