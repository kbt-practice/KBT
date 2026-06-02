package com.kbt.amumal.domain.comment.service;

import com.kbt.amumal.domain.comment.dto.CommentReqDTO;
import com.kbt.amumal.domain.comment.entity.Comment;
import com.kbt.amumal.domain.comment.repository.commentRepository;
import com.kbt.amumal.domain.post.entity.Post;
import com.kbt.amumal.domain.post.repository.PostRepository;
import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class commentService {
    private final commentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 생성
    public int create(String userId, Integer postId, CommentReqDTO.createComment request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글 또는 유저 정보를 확인해 주세요."));

        if (post.getDeletedAt() != null)
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 게시글입니다.");

        Comment newComment = commentRepository.save(Comment.builder()
                .content(request.getContent())
                .userId(userId)
                .postId(postId)
                .build());

        return newComment.getCommentId();
    }

    // 댓글 수정
    public void update(String userId, Integer commentId, CommentReqDTO.updateComment request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "댓글 또는 유저 정보를 확인해주세요."));

        if (comment.getDeletedAt() != null)
            throw new CustomException(ErrorCode.NOT_FOUND, "삭제된 댓글입니다.");

        if (!comment.getUserId().equals(userId))
            throw new CustomException(ErrorCode.FORBIDDEN, "유저 정보를 확인해주세요.");

        comment.updateComment(request.getContent());
    }

    // 댓글 삭제
    public void delete(String userId, Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "댓글 또는 유저 정보를 확인해주세요."));

        if (comment.getDeletedAt() != null)
            throw new CustomException(ErrorCode.NOT_FOUND, "이미 삭제된 댓글입니다.");

        if (!comment.getUserId().equals(userId))
            throw new CustomException(ErrorCode.FORBIDDEN, "유저 정보를 확인해주세요.");

        comment.softDelete();
    }
}
