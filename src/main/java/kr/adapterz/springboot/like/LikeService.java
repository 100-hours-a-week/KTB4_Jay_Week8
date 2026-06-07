package kr.adapterz.springboot.like;

import kr.adapterz.springboot.like.dto.LikeRequest;
import kr.adapterz.springboot.like.dto.LikeResponse;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 누르기
    public LikeResponse like(Long postId, LikeRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post_not_found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("post_not_found");
        }
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));
        if (likeRepository.existsByPostIdAndUserId(postId, request.getUserId())) {
            throw new IllegalArgumentException("already_liked");
        }

        likeRepository.save(new Like(postId, request.getUserId()));

        return new LikeResponse(likeRepository.countByPostId(postId));
    }

    // 좋아요 취소
    public LikeResponse unlike(Long postId, LikeRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post_not_found"));
        if (post.isDeleted()) {
            throw new IllegalArgumentException("post_not_found");
        }
        Like like = likeRepository.findByPostIdAndUserId(postId, request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("like_not_found"));

        likeRepository.delete(like);

        return new LikeResponse(likeRepository.countByPostId(postId));
    }
}
