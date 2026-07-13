package kr.adapterz.springboot.post;

import kr.adapterz.springboot.global.exception.ForbiddenException;
import kr.adapterz.springboot.like.LikeRepository;
import kr.adapterz.springboot.post.dto.PostDetailResponse;
import kr.adapterz.springboot.post.dto.PostRequest;
import kr.adapterz.springboot.post.dto.PostResponse;
import kr.adapterz.springboot.post.dto.UpdatePostRequest;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceUnitTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private kr.adapterz.springboot.comment.CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostViewRepository postViewRepository;

    @Mock
    private PostReader postReader;

    // ==============================
    // 게시글 작성
    // ==============================

    @Test
    void 게시글_작성_성공() {
        Long currentUserId = 1L;
        User user = user("test@test.com", "테스터", currentUserId);
        PostRequest request = postRequest("제목", "내용");
        Post savedPost = post(user, "제목", "내용", 10L);

        given(userReader.getActiveUser(currentUserId)).willReturn(user);
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        PostResponse response = postService.createPost(currentUserId, request);

        assertThat(response.getPostId()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("제목");
        assertThat(response.getContent()).isEqualTo("내용");
        assertThat(response.getAuthor()).isEqualTo("테스터");
    }

    // ==============================
    // 게시글 수정 권한
    // ==============================

    @Test
    void 작성자가_아니면_게시글_수정_실패() {
        User author = user("author@test.com", "작성자", 1L);
        Post post = post(author, "기존 제목", "기존 내용", 10L);
        UpdatePostRequest request = updatePostRequest("수정 제목", "수정 내용");

        given(postReader.getActivePost(10L)).willReturn(post);

        assertThatThrownBy(() -> postService.updatePost(2L, 10L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    // ==============================
    // 블라인드 게시글 상세 응답
    // ==============================

    @Test
    void 블라인드_게시글은_상세_내용을_숨긴다() {
        User author = user("author@test.com", "작성자", 1L);
        Post post = post(author, "신고된 제목", "신고된 내용", 10L);
        post.blind();

        given(postReader.getActivePostWithAuthor(10L)).willReturn(post);

        PostDetailResponse response = postService.getPostDetail(10L, 2L);

        assertThat(response.getBlinded()).isTrue();
        assertThat(response.getTitle()).isEqualTo("블라인드 처리된 게시글입니다.");
        assertThat(response.getContent()).isNull();
        assertThat(response.getLikeCount()).isNull();
    }

    private User user(String email, String nickname, Long id) {
        User user = new User(email, "encoded-password", nickname, null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post post(User author, String title, String content, Long id) {
        Post post = new Post(author, title, content);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private PostRequest postRequest(String title, String content) {
        return new PostRequest(title, content);
    }

    private UpdatePostRequest updatePostRequest(String title, String content) {
        return new UpdatePostRequest(title, content);
    }
}
