package kr.adapterz.springboot.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 id로 댓글과 대댓글 전체 조회
    List<Comment> findByPostId(Long postId);

    // 게시글 id로 일반 댓글만 조회
    List<Comment> findParentCommentsByPostId(Long postId);

    // 부모 댓글 id로 대댓글 조회
    List<Comment> findRepliesByParentCommentId(Long parentCommentId);

    // 삭제되지 않은 일반 댓글 개수 조회
    Long countByPostId(Long postId);
}
