package kr.adapterz.springboot.report;

import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ReportServiceTest {
    @Autowired
    public ReportService reportService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PostRepository postRepository;

    @Autowired
    public ReportRepository reportRepository;

    @Test
    void blinded_test(){
        User savedUser = userRepository.save(new User(
                "save@adapterz.kr",
                "1234",
                "saveUser",
                "profile.png"
        ));

        Post post = postRepository.save(new Post(
                savedUser,
                "test_title",
                "test_content"
        ));

        userRepository.save(new User("user1@adapterz.kr", "1234", "user1", null));
        userRepository.save(new User("user2@adapterz.kr", "1234", "user2", null));
        userRepository.save(new User("user3@adapterz.kr", "1234", "user3", null));
        userRepository.save(new User("user4@adapterz.kr", "1234", "user4", null));
        userRepository.save(new User("user5@adapterz.kr", "1234", "user5", null));

        // when
        reportService.reportPost(1L, post.getId(), new ReportRequest("부적절한 게시글"));
        reportService.reportPost(2L, post.getId(), new ReportRequest("부적절한 게시글"));
        reportService.reportPost(3L, post.getId(), new ReportRequest("부적절한 게시글"));
        reportService.reportPost(4L, post.getId(), new ReportRequest("부적절한 게시글"));

        ReportResponse response = reportService.reportPost(
                5L,
                post.getId(),
                new ReportRequest("부적절한 게시글")
        );

        // then
        assertThat(response.getReportCount()).isEqualTo(5L);
        assertThat(response.getBlinded()).isTrue();

        Post foundPost = postRepository.findById(post.getId()).get();
        assertThat(foundPost.isBlinded()).isTrue();

        assertThat(reportRepository.countByPost_Id(post.getId())).isEqualTo(5L);

    }
}
