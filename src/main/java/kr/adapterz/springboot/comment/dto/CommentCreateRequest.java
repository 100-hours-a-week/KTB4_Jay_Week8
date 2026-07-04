package kr.adapterz.springboot.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CommentCreateRequest {
    private String comment;
}
