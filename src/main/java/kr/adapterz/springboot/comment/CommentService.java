package kr.adapterz.springboot.comment;

import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.comment.dto.*;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ForbiddenException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public CommentCreateResponse createComment(Long postId, CommentCreateRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BadRequestException("no_content");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.isDeleted()) {
            throw new NotFoundException("post_not_found");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        Comment comment = new Comment(post, user, null, request.getComment());
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
    @Transactional
    public CommentUpdateResponse updateComment(Long commentId, CommentUpdateRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            throw new BadRequestException("no_content");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (comment.isDeleted() || comment.isReply()) {
            throw new NotFoundException("comment_not_found");
        }
        validateAuthor(comment, request.getUserId());

        comment.update(request.getComment());
        User user = userRepository.findById(comment.getAuthor().getId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        return new CommentUpdateResponse(
                comment.getId(),
                comment.getContent(),
                user.getNickname(),
                comment.getCreatedAt(),
                commentRepository.countByPostId(comment.getPost().getId())
        );
    }

    // 댓글 삭제
    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId, CommentDeleteRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (comment.isDeleted()) {
            throw new NotFoundException("comment_not_found");
        }
        validateAuthor(comment, request.getUserId());

        comment.delete();

        return new CommentDeleteResponse(
                comment.getId(),
                true,
                "삭제된 댓글입니다.",
                commentRepository.countByPostId(comment.getPost().getId())
        );
    }

    // 대댓글 작성
    @Transactional
    public ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request) {
        if (request.getReplyComment() == null || request.getReplyComment().isBlank()) {
            throw new BadRequestException("no_reply_content");
        }
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("comment_not_found"));
        if (parentComment.isDeleted() || parentComment.isReply()) {
            throw new NotFoundException("comment_not_found");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        // 생성자에 넣기 위한 post 생성
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new NotFoundException("post_not_found"));

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
    public ReplyCreateResponse updateReply(Long commentId, Long replyId, ReplyUpdateRequest request) {
        if (request.getReplyEditComment() == null || request.getReplyEditComment().isBlank()) {
            throw new BadRequestException("no_reply_edit_content");
        }
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new NotFoundException("reply_not_found"));
        if (reply.isDeleted() || !commentId.equals(reply.getParentComment().getId())) {
            throw new NotFoundException("reply_not_found");
        }
        validateAuthor(reply, request.getUserId());

        reply.update(request.getReplyEditComment());
        User user = userRepository.findById(reply.getAuthor().getId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));

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
