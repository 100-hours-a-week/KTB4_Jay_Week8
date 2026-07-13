package kr.adapterz.springboot.comment;

import kr.adapterz.springboot.comment.dto.CommentCreateRequest;
import kr.adapterz.springboot.comment.dto.CommentCreateResponse;
import kr.adapterz.springboot.comment.dto.CommentDeleteResponse;
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
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("comment@test.com", "encoded-password", "댓글작성자", null));
        post = postRepository.save(new Post(user, "댓글 게시글", "댓글 게시글 내용"));
    }

    // ==============================
    // 댓글 작성/삭제 통합 흐름
    // ==============================

    @Test
    void 댓글을_작성하고_삭제하면_게시글_댓글수가_증감한다() {
        CommentCreateResponse created = commentService.createComment(
                user.getId(),
                post.getId(),
                commentCreateRequest("댓글 내용")
        );

        assertThat(created.getCommentCount()).isEqualTo(1L);
        assertThat(post.getCommentCount()).isEqualTo(1L);

        CommentDeleteResponse deleted = commentService.deleteComment(user.getId(), created.getCommentId());

        assertThat(deleted.getDeleted()).isTrue();
        assertThat(deleted.getCommentCount()).isEqualTo(0L);
        assertThat(post.getCommentCount()).isEqualTo(0L);
    }

    private CommentCreateRequest commentCreateRequest(String content) {
        return new CommentCreateRequest(content);
    }
}
