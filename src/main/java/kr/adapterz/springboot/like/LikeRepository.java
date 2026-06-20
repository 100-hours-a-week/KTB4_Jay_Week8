package kr.adapterz.springboot.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface LikeRepository extends JpaRepository<Like, Long> {


    Optional<Like> findByPostIdAndUserId(Long postId, Long userId);

    // 같은 게시글에 이미 좋아요 눌렀는지 확인
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // 게시글 id로 좋아요 개수 조회
    Long countByPostId(Long postId);
}
