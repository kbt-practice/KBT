package com.kbt.amumal.domain.search.controller;

import com.kbt.amumal.domain.search.dto.PostSearchResDTO;
import com.kbt.amumal.domain.search.service.PostLikeSearchService;
import com.kbt.amumal.domain.search.service.PostSearchService;
import com.kbt.amumal.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "검색", description = "게시글 제목·본문·댓글 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search/posts")
@ConditionalOnProperty(name = "search.enabled", havingValue = "true")
public class PostSearchController {

    private final PostSearchService postSearchService;
    private final PostLikeSearchService postLikeSearchService;

    // ES 검색 (nori 분석 + fuzzy, 정확도/최신순 정렬, 커서 페이지네이션)
    @Operation(summary = "게시글 검색", description = "Nori 형태소 분석과 AUTO fuzzy를 사용해 제목·본문·댓글을 검색합니다.")
    @SecurityRequirements
    @GetMapping
    public ApiResponse<?> searchPosts(
            @Parameter(description = "검색어") @RequestParam String keyword,
            @Parameter(description = "정렬: accuracy 또는 latest") @RequestParam(defaultValue = "accuracy") String sort,
            @Parameter(description = "직전 페이지의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기(1~100)") @RequestParam(defaultValue = "10") int size
    ) {
        PostSearchResDTO.response response = postSearchService.search(keyword, sort, cursor, size);
        return ApiResponse.success("게시글 검색 성공", response);
    }

    // MySQL LIKE 풀스캔 검색 (ES와 결과·성능 비교용 baseline, 페이지네이션 없음)
    @Operation(summary = "게시글 검색 (LIKE, 비교용)", description = "ES 검색과 결과·성능을 비교하기 위해 title/content를 LIKE %keyword%로 검색합니다. 커서 페이지네이션은 지원하지 않습니다.")
    @SecurityRequirements
    @GetMapping("/like")
    public ApiResponse<?> searchPostsByLike(
            @Parameter(description = "검색어") @RequestParam String keyword,
            @Parameter(description = "결과 개수(1~100)") @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success("게시글 검색 성공(LIKE)", postLikeSearchService.search(keyword, size));
    }
}
