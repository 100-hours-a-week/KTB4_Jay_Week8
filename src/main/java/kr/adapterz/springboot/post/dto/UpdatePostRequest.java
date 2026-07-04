package kr.adapterz.springboot.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UpdatePostRequest {
    @NotBlank(message = "empty_title")
    private String title;

    @NotBlank(message = "empty_content")
    private String content;
}
