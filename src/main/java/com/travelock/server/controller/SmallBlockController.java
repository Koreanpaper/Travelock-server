package com.travelock.server.controller;

import com.travelock.server.domain.SmallBlock;
import com.travelock.server.service.SmallBlockService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/small")
@RequiredArgsConstructor
public class SmallBlockController {

    private final SmallBlockService smallBlockService;

    // 스몰블록 생성 (카카오에서 데이터 가져오기)
    @PostMapping("/search")
    public ResponseEntity<String> createSmallBlock(@RequestParam String keyword, @RequestParam Long middleBlockId) {
        try {
            smallBlockService.searchAndCreateSmallBlock(keyword, middleBlockId);
            return ResponseEntity.ok("스몰블록 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("스몰블록 생성 실패" + e.getMessage());
        }

    }

    // 전체 스몰블록 조회
    @GetMapping("/list")
    public ResponseEntity<List<SmallBlock>> getAllSmallBlocks() {
        List<SmallBlock> smallBlocks = smallBlockService.getAllSmallBlocks();
        return ResponseEntity.ok(smallBlocks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SmallBlock> getSmallBlockById(@PathVariable Long id) {
        SmallBlock smallBlock = smallBlockService.getSmallBlockById(id);
        return ResponseEntity.ok(smallBlock);
    }

}
