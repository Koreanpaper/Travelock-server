package com.travelock.server.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.travelock.server.domain.*;
import com.travelock.server.dto.course.daily_create.DailyCourseCreateDto;
import com.travelock.server.dto.course.daily_create.FullBlockDto;
import com.travelock.server.dto.course.daily_create.SmallBlockDto;
import com.travelock.server.exception.base_exceptions.BadRequestException;
import com.travelock.server.exception.base_exceptions.ResourceNotFoundException;
import com.travelock.server.exception.course.AddDailyCourseFavoriteException;
import com.travelock.server.exception.course.AddDailyCourseScrapException;
import com.travelock.server.exception.review.AddReviewException;
import com.travelock.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DailyCourseService {
    private final JPAQueryFactory query;
    private final DailyCourseRepository dailyCourseRepository;
    private final DailyCourseFavoriteRepository dailyCourseFavoriteRepository;
    private final DailyCourseScrapRepository dailyCourseScrapRepository;
    private final FullAndDailyCourseConnectRepository fullAndDailyCourseConnectRepository;
    private final FullBlockRepository fullBlockRepository;


    /**
     * 일자별 일정 조회 By DailyCourseId
     */
    public DailyCourse findDailyCourse(Long dailyCourseId) {
        QDailyCourse qDailyCourse = QDailyCourse.dailyCourse;
        DailyCourse dailyCourse = query
                .selectFrom(qDailyCourse)
                .where(qDailyCourse.dailyCourseId.eq(dailyCourseId))
                .fetchOne(); // 데이터가 없으면 빈리스트 반환

        if (dailyCourse == null) {
            throw new ResourceNotFoundException("Full Course not found by ID("+dailyCourseId+")");
        }

        return dailyCourse;
    }

    /**
     * 일자별 일정 생성
     * - 프론트에서 일일일정 확정시 저장됨.
     */
    @Transactional
    public DailyCourse saveDailyCourse(DailyCourseCreateDto createDto){


//        json 데이터 입력 예
//        DailyCourse = {
//                FullBlock = [
//                    {FID=1,  block_num, BID=1, MB={ID, place_name, place_id}, SB={ID, map_x, map+y, link_url, reference_count }},
//                    {FID=2,  block_num,  BID=2, MB={ID, place_name, place_id,}, SB={ID, map_x, map+y, link_url, reference_count }},
//                    {FID=3,  block_num,  BID=3, MB={ID, place_name, place_id }, SB={ID, map_x, map+y, link_url, reference_count }},
//                    {FID=4,  block_num,  BID=4, MB={ID, place_name, place_id}, SB={ID, map_x, map+y, link_url, reference_count }},
//                    {FID=5,  block_num, BID=5, MB={ID, place_name, place_id}, SB={ID, map_x, map+y, link_url, reference_count }},
//                ]
//        }

        if(createDto == null){
            throw new BadRequestException("createDto is null");
        }

        DailyCourse dailyCourse = new DailyCourse();
        QMember qMember = QMember.member;
        QBigBlock qBigBlock = QBigBlock.bigBlock;
        QMiddleBlock qMiddleBlock = QMiddleBlock.middleBlock;
        QSmallBlock qSmallBlock = QSmallBlock.smallBlock;
        QFullCourse qFullCourse = QFullCourse.fullCourse;

        //Map으로 중복순회 방지
        Map<Long, BigBlock> bigBlockMap = new HashMap<>();
        Map<Long, MiddleBlock> middleBlockMap = new HashMap<>();
        Map<String, SmallBlock> existingSmallBlockMap = new HashMap<>();

        List<FullBlockDto> fullBlockDtoList = createDto.getFullBlockDtoList();
        List<Long> bigBlockIdList = new ArrayList<>();
        List<Long> middleBlockIdList = new ArrayList<>();
        List<String> smaillBlockPlaceIdList = new ArrayList<>();
        List<FullBlock> fullBlocksToBatchSave = new ArrayList<>();

        FullCourse fullCourse = new FullCourse();
        Member member = new Member();

        // bigBlockId와 middleBlockId, smallBlock의 placeId를 각각 리스트에 추가
        for (FullBlockDto dto : fullBlockDtoList) {
            bigBlockIdList.add(dto.getBigBlockId());
            middleBlockIdList.add(dto.getMiddleBlockId());
            smaillBlockPlaceIdList.add(dto.getSmallBlockDto().getPlaceId());
        }

        // BigBlock과 MiddleBlock, SmallBlock, FullCourse를 조회 --------------------------------------------- DB SELECT(한방쿼리로 필요한 데이터 모두 가져오기)
        List<Tuple> list = query.select(qBigBlock, qMiddleBlock, qSmallBlock)
                .from(qBigBlock)
                .join(qMiddleBlock).on(qMiddleBlock.middleBlockId.in(middleBlockIdList))
                .leftJoin(qSmallBlock).on(qSmallBlock.placeId.in(smaillBlockPlaceIdList)) // LEFT JOIN으로 smallBlock이 없으면 null
                .where(qBigBlock.bigBlockId.in(bigBlockIdList))
                .fetch();


        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No matching data.");
        }

        Tuple firstTuple = list.get(0);
        member = firstTuple.get(qMember);
        fullCourse = firstTuple.get(qFullCourse);

        if (member == null) {
            throw new ResourceNotFoundException("Member not found.");
        }

        if (fullCourse == null) {
            throw new ResourceNotFoundException("FullCourse not found.");
        }

        // 조회된 BigBlock과 MiddleBlock, 이미 존재하는 SmallBlock 객체를 리스트에 추가
        for (Tuple tuple : list) {

            BigBlock bigBlock = tuple.get(qBigBlock);
            MiddleBlock middleBlock = tuple.get(qMiddleBlock);
            SmallBlock smallBlock = tuple.get(qSmallBlock);

            bigBlockMap.put(bigBlock.getBigBlockId(), bigBlock);
            middleBlockMap.put(middleBlock.getMiddleBlockId(), middleBlock);

            if (smallBlock != null) {
                // SmallBlock이 있을때만 처리
                existingSmallBlockMap.put(smallBlock.getPlaceId(), smallBlock);
            }
        }



        for (FullBlockDto fullBlockDto : fullBlockDtoList) {
            // FullBlock과 관련된 엔티티 생성 및 연관 설정
            FullBlock fullBlock = new FullBlock();
            SmallBlockDto smallBlockDto = fullBlockDto.getSmallBlockDto();

            SmallBlock smallBlock = existingSmallBlockMap.get(smallBlockDto.getPlaceId());

            // 존재하지 않으면 새로운 SmallBlock 생성
            if (smallBlock == null) {
                smallBlock = new SmallBlock();

                MiddleBlock middleBlock = middleBlockMap.get(fullBlockDto.getMiddleBlockId());

                if (middleBlock == null) {
                    throw new ResourceNotFoundException("MiddleBlock not found");
                }

//                // SmallBlock 엔티티 설정
//                smallBlock.createNewSmallBlock(
//                        smallBlockDto.getMapX(),
//                        smallBlockDto.getMapY(),
//                        smallBlockDto.getPlaceId(),
//                        middleBlock
//                );

                existingSmallBlockMap.put(smallBlock.getPlaceId(), smallBlock);
            }

            BigBlock bigBlock = bigBlockMap.get(fullBlockDto.getBigBlockId());

            if (bigBlock == null) {
                throw new ResourceNotFoundException("BigBlock not found");
            }

            fullBlock.newFullBlock(
                    bigBlock,
                    smallBlock.getMiddleBlock(),
                    smallBlock
            );

            fullBlocksToBatchSave.add(fullBlock);
        }

        // DailyCourse 설정
        dailyCourse.addDailyCourse(
            member
        );

        //FullBlock Batch 저장 ----------------------------------------------------------------------- DB INSERT ( 1 )
        fullBlockRepository.saveAll(fullBlocksToBatchSave);
        // Daily Course 저장 ------------------------------------------------------------------------- DB INSERT ( 1 )
        DailyCourse savedDailyCourse = dailyCourseRepository.save(dailyCourse);
        //연결객체 저장 -------------------------------------------------------------------------------- DB INSERT ( 1 )
        FullAndDailyCourseConnect connect = new FullAndDailyCourseConnect();
        connect.createNewConnect(member, fullCourse, savedDailyCourse, createDto.getDayNum());
        // Daily Course 저장 ------------------------------------------------------------------------- DB INSERT ( 1 )
        fullAndDailyCourseConnectRepository.save(connect);

        return savedDailyCourse;
    }


    /** 일일일정 수정*/
    public DailyCourse modifyDailyCourse(DailyCourseCreateDto request) {

        //생성과 똑같이 데이터 받아옴
        //1 -> 데이터의 생성자 ID가 같은경우 수정으로 진행 -> dailyblockconnect에서 순서를 비교하고 같은 순서인데 데이터가 다른경우 새로 생성 후 교체 -> 최종 일정저장
        //2 -> 데이터의 생성자 ID가 다른경우 신규 생성으로 진행

        // 수정할 DailyCourse객체 조회
        QDailyCourse qDailyCourse = QDailyCourse.dailyCourse;
        DailyCourse dailyCourse = query.selectFrom(qDailyCourse).where(qDailyCourse.dailyCourseId.eq(request.getDailyCourseId())).fetchOne();

        //수정요청된 FullBlock과 비교


        return null;
    }





    /**좋아요 설정*/
    public void setFavorite(Long dailyCourseId) {
        Long memberId = 1L;
        QMember qMember = QMember.member;
        QDailyCourse qDailyCourse = QDailyCourse.dailyCourse;
        QDailyCourseFavorite qDailyCourseFavorite = QDailyCourseFavorite.dailyCourseFavorite;

        Tuple tuple = query.select(qMember, qDailyCourse, qDailyCourseFavorite)
                .from(qMember)
                .join(qDailyCourse).on(qDailyCourse.dailyCourseId.eq(dailyCourseId))
                .leftJoin(qDailyCourseFavorite).on(qDailyCourseFavorite.dailyCourse.dailyCourseId.eq(dailyCourseId)
                        .and(qDailyCourseFavorite.member.memberId.eq(memberId)))
                .where(qMember.memberId.eq(memberId))
                .fetchOne();

        if (tuple == null || tuple.get(qMember) == null || tuple.get(qDailyCourse) == null) {
            throw new BadRequestException("Member or DailyCourse not found");
        }

        if(tuple.get(qDailyCourseFavorite) != null){
            throw new BadRequestException("Already added to favorite");
        }

        DailyCourseFavorite dailyCourseFavorite = new DailyCourseFavorite();

        dailyCourseFavorite.addFavorite(
                tuple.get(qMember),
                tuple.get(qDailyCourse)
        );

        try {
            dailyCourseFavoriteRepository.save(dailyCourseFavorite);
        } catch (Exception e) {
            log.error("Failed to add DailyCourseFavorite. ", e);
            throw new AddDailyCourseFavoriteException("Failed to save DailyCourseFavorite");
        }


    }

    /**스크랩 설정*/
    public void setScrap(Long dailyCourseId) {

        Long memberId = 1L;
        QMember qMember = QMember.member;
        QDailyCourse qDailyCourse = QDailyCourse.dailyCourse;
        QDailyCourseScrap qDailyCourseScrap = QDailyCourseScrap.dailyCourseScrap;

        Tuple tuple = query.select(qMember, qDailyCourse, qDailyCourseScrap)
                .from(qMember)
                .join(qDailyCourse).on(qDailyCourse.dailyCourseId.eq(dailyCourseId))
                .leftJoin(qDailyCourseScrap).on(qDailyCourseScrap.dailyCourse.dailyCourseId.eq(dailyCourseId)
                        .and(qDailyCourseScrap.member.memberId.eq(memberId)))
                .where(qMember.memberId.eq(memberId))
                .fetchOne();

        if (tuple == null || tuple.get(qMember) == null || tuple.get(qDailyCourse) == null) {
            throw new BadRequestException("Member or DailyCourse not found");
        }

        if(tuple.get(qDailyCourseScrap) != null){
            throw new BadRequestException("Already scraped");
        }

        DailyCourseScrap dailyCourseScrap = new DailyCourseScrap();

        dailyCourseScrap.addScrap(
                tuple.get(qMember),
                tuple.get(qDailyCourse)
        );

        try {
            dailyCourseScrapRepository.save(dailyCourseScrap);
        } catch (Exception e) {
            log.error("Failed to add DailyCourseScrap. ", e);
            throw new AddDailyCourseScrapException("Failed to save DailyCourseScrap");
        }
    }

    /**좋아요한 일일일정 목록*/
    public List<DailyCourseFavorite> getMyFavorites(Long memberId) {
        QDailyCourseFavorite qDailyCourseFavorite = QDailyCourseFavorite.dailyCourseFavorite;

        List<DailyCourseFavorite> dailyCourseFavorites = query
                .selectFrom(qDailyCourseFavorite)
                .where(qDailyCourseFavorite.member.memberId.eq(memberId))
                .fetch();

        if (dailyCourseFavorites == null) {
            throw new ResourceNotFoundException("DailyCourseFavorite not found with Member id: " + memberId);
        }

        return dailyCourseFavorites;
    }

    /**스크랩한 일일일정 목록*/
    public List<DailyCourseScrap> getMyScraps(Long memberId) {
        QDailyCourseScrap qDailyCourseFavorite = QDailyCourseScrap.dailyCourseScrap;

        List<DailyCourseScrap> dailyCourseScraps = query
                .selectFrom(qDailyCourseFavorite)
                .where(qDailyCourseFavorite.member.memberId.eq(memberId))
                .fetch();

        if (dailyCourseScraps == null) {
            throw new ResourceNotFoundException("DailyCourseScrap not found with Member id: " + memberId);
        }

        return dailyCourseScraps;

    }
}

