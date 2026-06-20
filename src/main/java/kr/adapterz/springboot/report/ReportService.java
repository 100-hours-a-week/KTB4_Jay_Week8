package kr.adapterz.springboot.report;

import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReportService {
    private static final long BLIND_REPORT_COUNT = 5L;

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReportResponse reportPost(Long postId, ReportRequest request) {
        if (request.getUserId() == null) {
            throw new BadRequestException("empty_user_id");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("post_not_found"));
        if (post.isDeleted()) {
            throw new NotFoundException("post_not_found");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("user_not_found"));
        if (reportRepository.existsByPostIdAndUserId(postId, request.getUserId())) {
            throw new ConflictException("already_reported");
        }

        Report report = reportRepository.save(new Report(post, user, request.getReason()));
        Long reportCount = reportRepository.countByPost_Id(postId);
        if (reportCount >= BLIND_REPORT_COUNT) {
            post.blind();
        }

        return new ReportResponse(
                report.getId(),
                postId,
                reportCount,
                post.isBlinded()
        );
    }
}
