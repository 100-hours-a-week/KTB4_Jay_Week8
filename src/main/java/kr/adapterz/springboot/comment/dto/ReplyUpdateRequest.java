package kr.adapterz.springboot.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReplyUpdateRequest {
    private Long userId;
    private String replyEditComment;
}
