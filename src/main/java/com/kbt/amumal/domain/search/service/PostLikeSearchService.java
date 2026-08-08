package com.kbt.amumal.domain.search.service;

import com.kbt.amumal.domain.post.dto.PostResDTO;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.domain.user.dto.UserProjection;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// ES 검색(PostSearchService)과 결과·성능을 비교하기 위한 LIKE 기반 baseline 구현
@Service
@RequiredArgsConstructor
public class PostLikeSearchService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // LIKE %keyword% 풀스캔으로 게시글을 찾아 카드 형태로 반환 (ES 없이도 동작하는 baseline 검색)
    @Transactional(readOnly = true)
    public List<PostResDTO.PostListItem> search(String keyword, int size) {
        if (keyword == null || keyword.isBlank())
            throw new CustomException(ErrorCode.BAD_REQUEST, "검색어를 입력해주세요.");
        if (size < MIN_SIZE || size > MAX_SIZE)
            throw new CustomException(ErrorCode.BAD_REQUEST, "size는 1에서 100 사이여야 합니다.");

        List<Post> posts = postRepository.searchByTitleOrContentLike(keyword.trim(), PageRequest.of(0, size));
        return hydratePostCards(posts);
    }

    // 조회된 게시글들의 작성자 정보를 한 번에 붙여 응답용 카드로 조립
    private List<PostResDTO.PostListItem> hydratePostCards(List<Post> posts) {
        List<Integer> authorIds = posts.stream().map(Post::getUserId).distinct().toList();
        Map<Integer, UserProjection> authorMap = userRepository.findProjectionsByIdIn(authorIds).stream()
                .collect(Collectors.toMap(UserProjection::id, Function.identity()));

        return posts.stream()
                .map(post -> {
                    UserProjection user = authorMap.get(post.getUserId());
                    return new PostResDTO.PostListItem(
                            post.getPostId(),
                            post.getTitle(),
                            post.getPostImageUrl(),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            post.getViewCount(),
                            user == null ? null : new PostResDTO.UserInfo(
                                    user.userId(), user.nickname(), user.profileImageUrl()
                            ),
                            post.getCreatedAt()
                    );
                })
                .toList();
    }
}
