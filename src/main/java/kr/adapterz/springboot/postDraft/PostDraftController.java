package kr.adapterz.springboot.postDraft;

import jakarta.validation.Valid;
import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import kr.adapterz.springboot.post.dto.PostResponse;
import kr.adapterz.springboot.postDraft.dto.DraftResponse;
import kr.adapterz.springboot.postDraft.dto.DraftSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/draft")
public class PostDraftController {

    private final PostDraftService postDraftService;

    @PostMapping
    public ResponseEntity<ApiResponse<DraftResponse>> saveDraft(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @Valid @RequestBody DraftSaveRequest request
    ) {
        DraftResponse response = postDraftService.saveDraft(
                customUserPrincipal.getUserId(),
                request
        );

        return ResponseEntity.ok(new ApiResponse<>("draft_save_success", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DraftResponse>> getDraft(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ) {
        DraftResponse response = postDraftService.getDraft(customUserPrincipal.getUserId());

        return ResponseEntity.ok(new ApiResponse<>("draft_get_success", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ) {
        postDraftService.deleteDraft(customUserPrincipal.getUserId());

        return ResponseEntity.ok(new ApiResponse<>("draft_delete_success", null));
    }

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<PostResponse>> publishDraft(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ) {
        PostResponse response = postDraftService.publishDraft(customUserPrincipal.getUserId());

        return ResponseEntity.ok(new ApiResponse<>("draft_publish_success", response));
    }
}
