package kr.adapterz.springboot.comment;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CommentRepository {
    private final Map<Long, Comment> store = new HashMap<>();
    private Long sequence = 1L;

    public Comment save(Comment comment) {
        comment.setId(sequence);
        store.put(sequence, comment);
        sequence++;

        return comment;
    }

    public Optional<Comment> findById(Long commentId) {
        return Optional.ofNullable(store.get(commentId));
    }

    // 게시글 id로 댓글과 대댓글 전체 조회
    public List<Comment> findByPostId(Long postId) {
        return store.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .toList();
    }

    // 게시글 id로 일반 댓글만 조회
    public List<Comment> findParentCommentsByPostId(Long postId) {
        return store.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .filter(comment -> comment.getParentCommentId() == null)
                .toList();
    }

    // 부모 댓글 id로 대댓글 조회
    public List<Comment> findRepliesByParentCommentId(Long parentCommentId) {
        return store.values().stream()
                .filter(comment -> parentCommentId.equals(comment.getParentCommentId()))
                .toList();
    }

    // 삭제되지 않은 일반 댓글 개수 조회
    public Long countByPostId(Long postId) {
        return store.values().stream()
                .filter(comment -> comment.getPostId().equals(postId))
                .filter(comment -> comment.getParentCommentId() == null)
                .filter(comment -> !comment.isDeleted())
                .count();
    }
}
