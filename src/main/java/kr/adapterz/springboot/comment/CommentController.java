package kr.adapterz.springboot.comment;

import kr.adapterz.springboot.comment.dto.*;
import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentCreateResponse>> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ) {
        CommentCreateResponse response = commentService.createComment(
                customUserPrincipal.getUserId(),
                postId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("comment_create_success", response));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentUpdateResponse>> updateComment(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        CommentUpdateResponse response = commentService.updateComment(
                customUserPrincipal.getUserId(),
                commentId,
                request
        );

        return ResponseEntity.ok()
                .body(new ApiResponse<>("comment_edit_success", response));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentDeleteResponse>> deleteComment(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long commentId
    ) {
        CommentDeleteResponse response = commentService.deleteComment(
                customUserPrincipal.getUserId(),
                commentId
        );

        return ResponseEntity.ok()
                .body(new ApiResponse<>("comment_delete_success", response));
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyCreateResponse>> createReply(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long commentId,
            @RequestBody ReplyCreateRequest request
    ) {
        ReplyCreateResponse response = commentService.createReply(
                customUserPrincipal.getUserId(),
                commentId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("reply_created_success", response));
    }

    @PatchMapping("/comments/{commentId}/replies/{replyId}")
    public ResponseEntity<ApiResponse<ReplyCreateResponse>> updateReply(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @RequestBody ReplyUpdateRequest request
    ) {
        ReplyCreateResponse response = commentService.updateReply(
                customUserPrincipal.getUserId(),
                commentId,
                replyId,
                request
        );

        return ResponseEntity.ok()
                .body(new ApiResponse<>("replyedit_created_success", response));
    }
}
