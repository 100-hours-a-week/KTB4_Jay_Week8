package kr.adapterz.springboot.like;

import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import kr.adapterz.springboot.like.dto.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> like(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long postId
    ) {
        LikeResponse response = likeService.like(
                customUserPrincipal.getUserId(),
                postId);

        return ResponseEntity.ok()
                .body(new ApiResponse<>("like_success", response));
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> unlike(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long postId
    ) {
        LikeResponse response = likeService.unlike(
                customUserPrincipal.getUserId(),
                postId);
        return ResponseEntity.ok()
                .body(new ApiResponse<>("unlike_success", response));
    }
}
