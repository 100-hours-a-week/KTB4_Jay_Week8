package kr.adapterz.springboot.postDraft.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DraftSaveRequest {
    private String title;

    private String content;

    @AssertTrue(message = "empty_draft")
    public boolean isNotEmptyDraft() {
        boolean emptyTitle = title == null || title.isBlank();
        boolean emptyContent = content == null || content.isBlank();
        return !emptyTitle || !emptyContent;
    }
}
