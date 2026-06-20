package kr.adapterz.springboot.like;

import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.like.dto.LikeRequest;
import kr.adapterz.springboot.like.dto.LikeResponse;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 누르기
    @Transactional
    public LikeResponse like(Long postId, LikeRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.isDeleted()) {
            throw new NotFoundException("post_not_found");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException(("User_not_found")));

        if (likeRepository.existsByPostIdAndUserId(postId, request.getUserId())) {
            throw new ConflictException("already_liked");
        }

        likeRepository.save(new Like(post, user));

        return new LikeResponse(likeRepository.countByPostId(postId));
    }

    // 좋아요 취소
    @Transactional
    public LikeResponse unlike(Long postId, LikeRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.isDeleted()) {
            throw new NotFoundException("post_already_deleted");
        }
        Like like = likeRepository.findByPostIdAndUserId(postId, request.getUserId())
                .orElseThrow(() -> new NotFoundException("like_not_found"));

        likeRepository.delete(like);

        return new LikeResponse(likeRepository.countByPostId(postId));
    }
}
