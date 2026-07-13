package kr.adapterz.springboot.report;

import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostReader;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
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
class ReportServiceUnitTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostReader postReader;

    @Mock
    private UserReader userReader;

    // ==============================
    // 중복 신고 검증
    // ==============================

    @Test
    void 이미_신고한_게시글이면_신고_실패() {
        Long userId = 1L;
        Long postId = 10L;
        User user = user(userId, "신고자");
        Post post = post(user, postId);

        given(postReader.getActivePost(postId)).willReturn(post);
        given(userReader.getActiveUser(userId)).willReturn(user);
        given(reportRepository.existsByPost_IdAndUser_Id(postId, userId)).willReturn(true);

        assertThatThrownBy(() -> reportService.reportPost(userId, postId, new ReportRequest("부적절한 게시글")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("already_reported");
    }

    // ==============================
    // 신고 누적 블라인드
    // ==============================

    @Test
    void 신고수가_5개_이상이면_게시글이_블라인드된다() {
        Long userId = 1L;
        Long postId = 10L;
        User user = user(userId, "신고자");
        Post post = post(user, postId);
        Report savedReport = report(post, user, 100L);

        given(postReader.getActivePost(postId)).willReturn(post);
        given(userReader.getActiveUser(userId)).willReturn(user);
        given(reportRepository.existsByPost_IdAndUser_Id(postId, userId)).willReturn(false);
        given(reportRepository.save(any(Report.class))).willReturn(savedReport);
        given(reportRepository.countByPost_Id(postId)).willReturn(5L);

        ReportResponse response = reportService.reportPost(userId, postId, new ReportRequest("부적절한 게시글"));

        assertThat(response.getReportCount()).isEqualTo(5L);
        assertThat(response.getBlinded()).isTrue();
        assertThat(post.isBlinded()).isTrue();
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

    private Report report(Post post, User user, Long id) {
        Report report = new Report(post, user, "부적절한 게시글");
        ReflectionTestUtils.setField(report, "id", id);
        return report;
    }
}
