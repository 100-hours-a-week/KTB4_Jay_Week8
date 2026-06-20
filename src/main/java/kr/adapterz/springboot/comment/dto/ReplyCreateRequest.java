package kr.adapterz.springboot.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReplyCreateRequest {
    private Long userId;
    private Long postId;
    private String replyComment;
}
