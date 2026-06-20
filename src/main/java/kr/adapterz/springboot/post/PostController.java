package kr.adapterz.springboot.post;

import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.post.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/posts")
@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> post(
            @RequestBody PostRequest request
    ){
        PostResponse response = postService.post(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("post_created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponse>>> postlist(){
        List<PostListResponse> response = postService.getPost();

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
            @PathVariable Long postId,
            @RequestBody DeletePostRequest request
    ) {
        postService.deletePost(postId, request  );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<UpdatePostResponse>> updatePost(
            @PathVariable Long postId,
            @RequestBody UpdatePostRequest request
            ){
        UpdatePostResponse response = postService.updatePost(postId, request);
        return ResponseEntity.ok()
                .body(new ApiResponse<>("post_updated", response));
    }
}
