package kr.adapterz.springboot.comment;

import kr.adapterz.springboot.comment.dto.CommentCreateRequest;
import kr.adapterz.springboot.comment.dto.CommentUpdateRequest;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ForbiddenException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostReader;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostReader postReader;

    @Mock
    private UserReader userReader;

    // ==============================
    // 댓글 작성 검증
    // ==============================

    @Test
    void 댓글_내용이_비어있으면_작성_실패() {
        CommentCreateRequest request = commentCreateRequest("");

        assertThatThrownBy(() -> commentService.createComment(1L, 10L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("no_content");
    }

    // ==============================
    // 댓글 수정 권한
    // ==============================

    @Test
    void 작성자가_아니면_댓글_수정_실패() {
        User author = user(1L, "작성자");
        Post post = post(author);
        Comment comment = comment(post, author, null, "기존 댓글", 100L);
        CommentUpdateRequest request = commentUpdateRequest("수정 댓글");

        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.updateComment(2L, 100L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    private User user(Long id, String nickname) {
        User user = new User(nickname + "@test.com", "encoded-password", nickname, null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post post(User author) {
        Post post = new Post(author, "제목", "내용");
        ReflectionTestUtils.setField(post, "id", 10L);
        return post;
    }

    private Comment comment(Post post, User author, Comment parent, String content, Long id) {
        Comment comment = new Comment(post, author, parent, content);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    private CommentCreateRequest commentCreateRequest(String content) {
        return new CommentCreateRequest(content);
    }

    private CommentUpdateRequest commentUpdateRequest(String content) {
        return new CommentUpdateRequest(content);
    }
}
