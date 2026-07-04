package kr.adapterz.springboot.postDraft;

import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.NotFoundException;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.post.dto.PostResponse;
import kr.adapterz.springboot.postDraft.dto.DraftResponse;
import kr.adapterz.springboot.postDraft.dto.DraftSaveRequest;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserReader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostDraftService {

    private final PostDraftRepository postDraftRepository;
    private final UserReader userReader;
    private final PostRepository postRepository;

    @Transactional
    public DraftResponse saveDraft(
            Long currentUserId,
            DraftSaveRequest request
    ) {
        User user = userReader.getActiveUser(currentUserId);

        PostDraft draft = postDraftRepository.findByUser_Id(currentUserId)
                .orElseGet(() -> new PostDraft(user, request.getTitle(), request.getContent()));

        if (draft.getId() != null) {
            draft.update(request.getTitle(), request.getContent());
        } else {
            postDraftRepository.save(draft);
        }

        return new DraftResponse(
                draft.getId(),
                draft.getTitle(),
                draft.getContent(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }

    public DraftResponse getDraft(Long currentUserId) {
        PostDraft draft = postDraftRepository.findByUser_Id(currentUserId)
                .orElseThrow(() -> new NotFoundException("draft_not_found"));

        return new DraftResponse(
                draft.getId(),
                draft.getTitle(),
                draft.getContent(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteDraft(Long currentUserId) {
        PostDraft draft = postDraftRepository.findByUser_Id(currentUserId)
                .orElseThrow(() -> new NotFoundException("draft_not_found"));

        postDraftRepository.delete(draft);
    }

    @Transactional
    public PostResponse publishDraft(Long currentUserId) {
        PostDraft draft = postDraftRepository.findByUser_Id(currentUserId)
                .orElseThrow(() -> new NotFoundException("draft_not_found"));

        if (draft.getTitle() == null || draft.getTitle().isBlank()) {
            throw new BadRequestException("empty_title");
        }

        if (draft.getContent() == null || draft.getContent().isBlank()) {
            throw new BadRequestException("empty_content");
        }

        User user = draft.getUser();

        Post post = new Post(user, draft.getTitle(), draft.getContent());
        Post savedPost = postRepository.save(post);

        postDraftRepository.delete(draft);

        return new PostResponse(
                savedPost.getId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                user.getNickname(),
                savedPost.getCreatedAt()
        );
    }
}
