package kr.adapterz.springboot.postDraft;

import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.post.dto.PostResponse;
import kr.adapterz.springboot.postDraft.dto.DraftResponse;
import kr.adapterz.springboot.postDraft.dto.DraftSaveRequest;
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
class PostDraftServiceIntegrationTest {

    @Autowired
    private PostDraftService postDraftService;

    @Autowired
    private PostDraftRepository postDraftRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("draft@test.com", "encoded-password", "임시저장유저", null));
    }

    // ==============================
    // 임시저장 저장/조회
    // ==============================

    @Test
    void 제목만_있어도_임시저장이_가능하다() {
        DraftResponse response = postDraftService.saveDraft(
                user.getId(),
                new DraftSaveRequest("제목만 있는 임시저장", "")
        );

        DraftResponse foundDraft = postDraftService.getDraft(user.getId());

        assertThat(response.getDraftId()).isNotNull();
        assertThat(foundDraft.getTitle()).isEqualTo("제목만 있는 임시저장");
        assertThat(foundDraft.getContent()).isEqualTo("");
    }

    // ==============================
    // 임시저장 발행
    // ==============================

    @Test
    void 임시저장을_발행하면_게시글이_생성되고_임시저장은_삭제된다() {
        postDraftService.saveDraft(
                user.getId(),
                new DraftSaveRequest("발행 제목", "발행 내용")
        );

        PostResponse response = postDraftService.publishDraft(user.getId());

        assertThat(response.getPostId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("발행 제목");
        assertThat(postRepository.findById(response.getPostId())).isPresent();
        assertThat(postDraftRepository.findByUser_Id(user.getId())).isEmpty();
    }
}
