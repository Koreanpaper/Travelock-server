package com.travelock.server.controller;

import com.travelock.server.domain.MiddleBlock;
import com.travelock.server.service.MiddleBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/middle")
@RequiredArgsConstructor
public class MiddleBlockController {

    private final MiddleBlockService middleBlockService;

    // 카테고리 목록 조회
    @GetMapping("/category")
    public List<MiddleBlock> getAllMiddleBlocks() {
        return middleBlockService.getAllCategories();
    }

    // 특정 카테고리 코드로 조회 ->
    @GetMapping("/{categoryCode}")
    public MiddleBlock getMiddleBlockByCategoryCode(@PathVariable String categoryCode) {
        return middleBlockService.getCategoryByCode(categoryCode);
    }
}
