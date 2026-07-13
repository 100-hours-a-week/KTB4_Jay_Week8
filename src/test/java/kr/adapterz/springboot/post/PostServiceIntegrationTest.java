package kr.adapterz.springboot.post;

import kr.adapterz.springboot.post.dto.PostDetailResponse;
import kr.adapterz.springboot.post.dto.PostRequest;
import kr.adapterz.springboot.post.dto.PostResponse;
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
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("post@test.com", "encoded-password", "게시글작성자", null));
    }

    // ==============================
    // 게시글 작성 통합 흐름
    // ==============================

    @Test
    void 게시글_작성하면_DB에_저장된다() {
        PostResponse response = postService.createPost(
                user.getId(),
                postRequest("통합 제목", "통합 내용")
        );

        Post savedPost = postRepository.findById(response.getPostId())
                .orElseThrow();

        assertThat(savedPost.getTitle()).isEqualTo("통합 제목");
        assertThat(savedPost.getContent()).isEqualTo("통합 내용");
        assertThat(savedPost.getAuthor().getId()).isEqualTo(user.getId());
    }

    // ==============================
    // 조회수 24시간 중복 방지
    // ==============================

    @Test
    void 같은_사용자가_24시간_안에_다시_조회하면_조회수가_증가하지_않는다() {
        PostResponse created = postService.createPost(
                user.getId(),
                postRequest("조회수 제목", "조회수 내용")
        );

        PostDetailResponse first = postService.getPostDetail(created.getPostId(), user.getId());
        PostDetailResponse second = postService.getPostDetail(created.getPostId(), user.getId());

        assertThat(first.getViewCount()).isEqualTo(1L);
        assertThat(second.getViewCount()).isEqualTo(1L);
    }

    private PostRequest postRequest(String title, String content) {
        return new PostRequest(title, content);
    }
}
