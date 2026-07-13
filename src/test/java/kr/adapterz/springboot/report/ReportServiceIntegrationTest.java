package kr.adapterz.springboot.report;

import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
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
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        User author = userRepository.save(new User("report-author@test.com", "encoded-password", "신고게시글작성자", null));
        post = postRepository.save(new Post(author, "신고 게시글", "신고 게시글 내용"));
    }

    // ==============================
    // 신고 누적 통합 흐름
    // ==============================

    @Test
    void 서로_다른_사용자_5명이_신고하면_게시글이_블라인드된다() {
        ReportResponse lastResponse = null;

        for (int i = 1; i <= 5; i++) {
            User reporter = userRepository.save(new User("reporter" + i + "@test.com", "encoded-password", "신고자" + i, null));
            lastResponse = reportService.reportPost(reporter.getId(), post.getId(), new ReportRequest("부적절한 게시글"));
        }

        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getReportCount()).isEqualTo(5L);
        assertThat(lastResponse.getBlinded()).isTrue();
        assertThat(post.isBlinded()).isTrue();
    }
}
