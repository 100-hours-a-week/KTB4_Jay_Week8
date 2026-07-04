package kr.adapterz.springboot.comment;

import kr.adapterz.springboot.post.PostReader;
import kr.adapterz.springboot.user.UserReader;
import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.comment.dto.*;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ForbiddenException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostReader postReader;
    private final UserReader userReader;

    // 댓글 작성
    @Transactional
    public CommentCreateResponse createComment(
            Long currentUserId,
            Long postId,
            CommentCreateRequest request
    ){
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BadRequestException("no_content");
        }
        Post post = postReader.getActivePost(postId);
        post.commentIncrease();
        User user = userReader.getActiveUser(currentUserId);

        Comment comment = new Comment(post, user, null, request.getComment());
        Comment savedComment = commentRepository.save(comment);

        return new CommentCreateResponse(
                savedComment.getId(),
                savedComment.getAuthor().getId(),
                savedComment.getContent(),
                user.getNickname(),
                savedComment.getCreatedAt(),
                post.getCommentCount()
        );
    }

    // 댓글 수정
    @Transactional
    public CommentUpdateResponse updateComment(
            Long currentUserId,
            Long commentId,
            CommentUpdateRequest request
    ){
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BadRequestException("no_content");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (comment.isDeleted() || comment.isReply()) {
            throw new NotFoundException("comment_not_found");
        }
        validateAuthor(comment, currentUserId);

        comment.update(request.getComment());
        User user = userReader.getActiveUser(currentUserId);
        return new CommentUpdateResponse(
                comment.getId(),
                user.getId(),
                comment.getContent(),
                user.getNickname(),
                comment.getCreatedAt(),
                comment.getPost().getCommentCount()
        );
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponse deleteComment(
            Long currentUserId,
            Long commentId
    ){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (comment.isDeleted()) {
            throw new NotFoundException("comment_not_found");
        }
        validateAuthor(comment, currentUserId);
        Post post = comment.getPost();
        comment.delete();
        if (!comment.isReply()){
            post.commentDecrease();
        }


        return new CommentDeleteResponse(
                comment.getId(),
                true,
                "삭제된 댓글입니다.",
                comment.getPost().getCommentCount()
        );
    }

    // 대댓글 작성
    @Transactional
    public ReplyCreateResponse createReply(
            Long currentUserId,
            Long commentId,
            ReplyCreateRequest request
    ) {
        if (request.getReplyComment() == null || request.getReplyComment().isBlank()) {
            throw new BadRequestException("no_reply_content");
        }
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (parentComment.isDeleted() || parentComment.isReply()) {
            throw new NotFoundException("comment_not_found");
        }
        User user = userReader.getActiveUser(currentUserId);

        // 생성자에 넣기 위한 post 생성
        Post post = parentComment.getPost();

        Comment reply = new Comment(post, user, parentComment, request.getReplyComment());
        Comment savedReply = commentRepository.save(reply);

        return new ReplyCreateResponse(
                savedReply.getId(),
                parentComment.getId(),
                savedReply.getContent(),
                user.getNickname(),
                user.isDeleted(),
                savedReply.getCreatedAt()
        );
    }

    // 대댓글 수정
    @Transactional
    public ReplyCreateResponse updateReply(
            Long currentUserId,
            Long commentId,
            Long replyId,
            ReplyUpdateRequest request
    ) {
        if (request.getReplyEditComment() == null || request.getReplyEditComment().isBlank()) {
            throw new BadRequestException("no_reply_edit_content");
        }
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("reply_not_found"));
        if (reply.isDeleted() || !commentId.equals(reply.getParentComment().getId())) {
            throw new NotFoundException("reply_not_found");
        }
        validateAuthor(reply, currentUserId);

        reply.update(request.getReplyEditComment());
        User user = userReader.getActiveUser(currentUserId);

        return new ReplyCreateResponse(
                reply.getId(),
                commentId,
                reply.getContent(),
                user.getNickname(),
                user.isDeleted(),
                reply.getCreatedAt()
        );
    }

    // 작성자 본인인지 확인
    private void validateAuthor(Comment comment, Long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException();
        }
    }
}
