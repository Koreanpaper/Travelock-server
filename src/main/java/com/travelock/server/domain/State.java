package com.travelock.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.travelock.server.domain.BigBlock;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class State{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stateId;  // 고유 ID

    @Column(columnDefinition = "VARCHAR(5) COMMENT '도/광역시/특별시 코드(2자리)'")
    private String stateCode;  // 시/도 코드 (2자리)

    @Column(columnDefinition = "VARCHAR(50) COMMENT '도/광역시/특별시 이름'")
    private String stateName;  // 시/도 이름

    // State가 여러 BigBlock을 참조하는 관계 (1:N)
    @OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BigBlock> bigBlockList = new ArrayList<>();  // BigBlock 리스트 초기화

//    // 추후에 bigBlock 추가하는 메서드 (state 안에 city가 있으니까)
//    public void addBigBlock(BigBlock bigBlock) {
//        bigBlockList.add(bigBlock);
//        bigBlock.setState(this); // state와 양방향 관계
//    }
//
//    // BigBlock 제거하는 메서드 구현만
//    public void removeBigBlock(BigBlock bigBlock) {
//        bigBlockList.remove(bigBlock);
//        bigBlock.setState(null); // 양방향 관계도 해제
//    }
}