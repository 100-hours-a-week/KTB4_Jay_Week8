package kr.adapterz.springboot.like;

import kr.adapterz.springboot.like.dto.LikeResponse;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("like@test.com", "encoded-password", "좋아요유저", null));
        post = postRepository.save(new Post(user, "좋아요 게시글", "좋아요 게시글 내용"));
    }

    // ==============================
    // 좋아요/취소 통합 흐름
    // ==============================

    @Test
    void 좋아요를_누르고_취소하면_좋아요수가_변한다() {
        LikeResponse liked = likeService.like(user.getId(), post.getId());

        assertThat(liked.getLikeCount()).isEqualTo(1L);
        assertThat(likeRepository.existsByPost_IdAndUser_Id(post.getId(), user.getId())).isTrue();

        LikeResponse unliked = likeService.unlike(user.getId(), post.getId());

        assertThat(unliked.getLikeCount()).isEqualTo(0L);
        assertThat(likeRepository.existsByPost_IdAndUser_Id(post.getId(), user.getId())).isFalse();
    }
}
