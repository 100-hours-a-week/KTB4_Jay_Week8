package kr.adapterz.springboot.like;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LikeRepository {
    private final Map<Long, Like> store = new HashMap<>();
    private Long sequence = 1L;

    public Like save(Like like) {
        like.setId(sequence);
        store.put(sequence, like);
        sequence++;

        return like;
    }

    public Optional<Like> findByPostIdAndUserId(Long postId, Long userId) {
        return store.values().stream()
                .filter(like -> like.getPostId().equals(postId))
                .filter(like -> like.getUserId().equals(userId))
                .findFirst();
    }

    // 같은 게시글에 이미 좋아요 눌렀는지 확인
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return findByPostIdAndUserId(postId, userId).isPresent();
    }

    public void delete(Like like) {
        store.remove(like.getId());
    }

    // 게시글 id로 좋아요 개수 조회
    public Long countByPostId(Long postId) {
        return store.values().stream()
                .filter(like -> like.getPostId().equals(postId))
                .count();
    }
}
