package kr.adapterz.springboot.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CommentUpdateRequest {
    private String comment;
}
