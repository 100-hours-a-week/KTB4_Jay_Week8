package kr.adapterz.springboot.comment;

import kr.adapterz.springboot.comment.dto.*;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    public CommentCreateResponse createComment(Long postId, CommentCreateRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new IllegalArgumentException("no_content");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post_not_found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("post_not_found");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));

        Comment comment = new Comment(postId, user.getId(), null, request.getComment());
        Comment savedComment = commentRepository.save(comment);

        return new CommentCreateResponse(
                savedComment.getId(),
                savedComment.getContent(),
                user.getNickname(),
                savedComment.getCreatedAt(),
                commentRepository.countByPostId(postId)
        );
    }

    // 댓글 수정
    public CommentUpdateResponse updateComment(Long commentId, CommentUpdateRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new IllegalArgumentException("no_content");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment_not_found"));
        if (comment.isDeleted() || comment.isReply()) {
            throw new IllegalArgumentException("comment_not_found");
        }
        validateAuthor(comment, request.getUserId());

        comment.update(request.getComment());
        User user = userRepository.findById(comment.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));

        return new CommentUpdateResponse(
                comment.getId(),
                comment.getContent(),
                user.getNickname(),
                comment.getCreatedAt(),
                commentRepository.countByPostId(comment.getPostId())
        );
    }

    // 댓글 삭제
    public CommentDeleteResponse deleteComment(Long commentId, CommentDeleteRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment_not_found"));
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("comment_not_found");
        }
        validateAuthor(comment, request.getUserId());

        comment.delete();

        return new CommentDeleteResponse(
                comment.getId(),
                true,
                "삭제된 댓글입니다.",
                commentRepository.countByPostId(comment.getPostId())
        );
    }

    // 대댓글 작성
    public ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request) {
        if (request.getReplyComment() == null || request.getReplyComment().isBlank()) {
            throw new IllegalArgumentException("no_reply_content");
        }
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("comment_not_found"));
        if (parentComment.isDeleted() || parentComment.isReply()) {
            throw new IllegalArgumentException("comment_not_found");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));

        Comment reply = new Comment(parentComment.getPostId(), user.getId(), parentComment.getId(), request.getReplyComment());
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
    public ReplyCreateResponse updateReply(Long commentId, Long replyId, ReplyUpdateRequest request) {
        if (request.getReplyEditComment() == null || request.getReplyEditComment().isBlank()) {
            throw new IllegalArgumentException("no_reply_edit_content");
        }
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("reply_not_found"));
        if (reply.isDeleted() || !commentId.equals(reply.getParentCommentId())) {
            throw new IllegalArgumentException("reply_not_found");
        }
        validateAuthor(reply, request.getUserId());

        reply.update(request.getReplyEditComment());
        User user = userRepository.findById(reply.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));

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
        if (userId == null || !comment.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("not_author");
        }
    }
}
