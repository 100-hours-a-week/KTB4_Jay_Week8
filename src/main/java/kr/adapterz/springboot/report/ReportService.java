package kr.adapterz.springboot.report;

import kr.adapterz.springboot.post.PostReader;
import kr.adapterz.springboot.user.UserReader;
import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
import kr.adapterz.springboot.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReportService {
    private static final long BLIND_REPORT_COUNT = 5L;

    private final ReportRepository reportRepository;
    private final PostReader postReader;
    private final UserReader userReader;

    @Transactional
    public ReportResponse reportPost(
            Long currentUserId,
            Long postId,
            ReportRequest request) {
        if (currentUserId == null) {
            throw new BadRequestException("empty_user_id");
        }
        Post post = postReader.getActivePost(postId);
        User user = userReader.getActiveUser(currentUserId);

        if (reportRepository.existsByPost_IdAndUser_Id(postId, currentUserId)) {
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
