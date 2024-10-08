package com.travelock.server.dto;

import com.travelock.server.domain.SmallBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmallBlockResponseDTO {
    private Long smallBlockId;
    private String placeId;
    private String mapX;
    private String mapY;
    private Integer referenceCount;

    public static SmallBlockResponseDTO fromDomainToResponseDTO(SmallBlock smallBlock) {
        if (smallBlock == null) {
            return null;  // SmallBlock이 null일 경우 null 반환
        }

        return new SmallBlockResponseDTO(
                smallBlock.getSmallBlockId(),
                smallBlock.getPlaceId(),
                smallBlock.getMapX(),
                smallBlock.getMapY(),
                smallBlock.getReferenceCount()
        );
    }
}
