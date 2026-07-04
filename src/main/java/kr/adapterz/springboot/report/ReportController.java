package kr.adapterz.springboot.report;

import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import kr.adapterz.springboot.report.dto.ReportRequest;
import kr.adapterz.springboot.report.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> reportPost(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long postId,
            @RequestBody ReportRequest request
    ) {
        ReportResponse response = reportService.reportPost(
                customUserPrincipal.getUserId(),
                postId,
                request);

        return ResponseEntity.ok()
                .body(new ApiResponse<>("report_success", response));
    }
}
