package kr.adapterz.springboot.like;

import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.NotFoundException;
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
class LikeServiceUnitTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostReader postReader;

    @Mock
    private UserReader userReader;

    // ==============================
    // 좋아요 중복 검증
    // ==============================

    @Test
    void 이미_좋아요를_눌렀으면_좋아요_실패() {
        Long userId = 1L;
        Long postId = 10L;
        User user = user(userId, "좋아요유저");
        Post post = post(user, postId);

        given(postReader.getActivePost(postId)).willReturn(post);
        given(userReader.getActiveUser(userId)).willReturn(user);
        given(likeRepository.existsByPost_IdAndUser_Id(postId, userId)).willReturn(true);

        assertThatThrownBy(() -> likeService.like(userId, postId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("already_liked");
    }

    // ==============================
    // 좋아요 취소 검증
    // ==============================

    @Test
    void 좋아요가_없으면_좋아요_취소_실패() {
        Long userId = 1L;
        Long postId = 10L;

        given(likeRepository.findByPost_IdAndUser_Id(postId, userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.unlike(userId, postId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("like_not_found");
    }

    private User user(Long id, String nickname) {
        User user = new User(nickname + "@test.com", "encoded-password", nickname, null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Post post(User author, Long id) {
        Post post = new Post(author, "제목", "내용");
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
