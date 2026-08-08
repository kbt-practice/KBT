package com.kbt.amumal.domain.search.dto;

import com.kbt.amumal.domain.post.dto.PostResDTO;

import java.util.List;

public class PostSearchResDTO {

    public record response(
            List<PostResDTO.PostListItem> posts,
            pagination pagination
    ) {}

    public record pagination(
            String nextCursor,
            boolean hasNext
    ) {}
}
