package kr.adapterz.springboot.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ReplyCreateRequest {
    private Long postId;
    private String replyComment;
}
