package kr.adapterz.springboot.postDraft;

import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.postDraft.dto.DraftSaveRequest;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostDraftServiceUnitTest {

    @InjectMocks
    private PostDraftService postDraftService;

    @Mock
    private PostDraftRepository postDraftRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private PostRepository postRepository;

    // ==============================
    // 임시저장 조회
    // ==============================

    @Test
    void 임시저장이_없으면_조회_실패() {
        Long userId = 1L;
        given(postDraftRepository.findByUser_Id(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postDraftService.getDraft(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("draft_not_found");
    }

    // ==============================
    // 기존 임시저장 수정
    // ==============================

    @Test
    void 기존_임시저장이_있으면_새로_저장하지_않고_수정한다() {
        Long userId = 1L;
        User user = user(userId, "임시저장유저");
        PostDraft draft = draft(user, "기존 제목", "기존 내용", 10L);
        DraftSaveRequest request = new DraftSaveRequest("수정 제목", "수정 내용");

        given(userReader.getActiveUser(userId)).willReturn(user);
        given(postDraftRepository.findByUser_Id(userId)).willReturn(Optional.of(draft));

        postDraftService.saveDraft(userId, request);

        assertThat(draft.getTitle()).isEqualTo("수정 제목");
        assertThat(draft.getContent()).isEqualTo("수정 내용");
        verify(postDraftRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ==============================
    // 임시저장 발행 검증
    // ==============================

    @Test
    void 제목이_비어있는_임시저장은_발행_실패() {
        Long userId = 1L;
        User user = user(userId, "임시저장유저");
        PostDraft draft = draft(user, "", "내용", 10L);

        given(postDraftRepository.findByUser_Id(userId)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> postDraftService.publishDraft(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("empty_title");
    }

    private User user(Long id, String nickname) {
        User user = new User(nickname + "@test.com", "encoded-password", nickname, null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private PostDraft draft(User user, String title, String content, Long id) {
        PostDraft draft = new PostDraft(user, title, content);
        ReflectionTestUtils.setField(draft, "id", id);
        return draft;
    }
}
