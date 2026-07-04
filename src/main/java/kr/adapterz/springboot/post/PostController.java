package kr.adapterz.springboot.post;

import jakarta.validation.Valid;
import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import kr.adapterz.springboot.post.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/posts")
@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @Valid @RequestBody PostRequest request
    ){
        PostResponse response = postService.createPost(customUserPrincipal.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("post_created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> postlist(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<PostListResponse> response = postService.getPost(pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>("post_list_success", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> postDetail(
            @PathVariable Long postId,
            @RequestParam Long userId
    ){
        PostDetailResponse response = postService.getPostDetail(postId, userId);

        return ResponseEntity.ok()
                .body(new ApiResponse<>("post_detail_success", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long postId
    ) {
        postService.deletePost(
                customUserPrincipal.getUserId(),
                postId
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
            ){
        UpdatePostResponse response = postService.updatePost(
                customUserPrincipal.getUserId(),
                postId,
                request
        );
        return ResponseEntity.ok()
                .body(new ApiResponse<>("post_updated", response));
    }
}
