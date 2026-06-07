package kr.adapterz.springboot.post;

import kr.adapterz.springboot.comment.Comment;
import kr.adapterz.springboot.comment.CommentRepository;
import kr.adapterz.springboot.comment.dto.CommentDetailResponse;
import kr.adapterz.springboot.comment.dto.ReplyCreateResponse;
import kr.adapterz.springboot.like.LikeRepository;
import kr.adapterz.springboot.post.dto.*;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostResponse post(PostRequest request){
        if (request.getTitle() == null || request.getTitle().isBlank()){
            throw new IllegalArgumentException("empty_title");
        }
        if (request.getContent() == null || request.getContent().isBlank()){
            throw new IllegalArgumentException("empty_content");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));
        Post post = new Post(
            user.getId(),
                request.getTitle(),
                request.getContent()
        );

        Post savedPost = postRepository.save(post);
        return new PostResponse(
                savedPost.getId(),
                savedPost.getTitle(),
                savedPost.getContent(),
                user.getNickname(),
                savedPost.getCreatedAt()
        );
    }
    public List<PostListResponse> getPost() {
        List<Post> posts = postRepository.findAll();

        return posts.stream()
                .filter(post -> !post.isDeleted())
                .map(post -> {
                    User author = userRepository.findById(post.getAuthorId())
                            .orElseThrow(() -> new IllegalArgumentException("No_User"));
                    if (post.isBlinded()) {
                        return new PostListResponse(
                                post.getId(),
                                "블라인드 처리된 게시글입니다.",
                                "블라인드 처리된 사용자입니다.",
                                null,
                                null,
                                null,
                                post.getCreatedAt(),
                                author.isDeleted(),
                                true
                        );
                    }
                    String authorNickname = author.isDeleted()
                            ? "알 수 없음"
                            : author.getNickname();
                    return new PostListResponse(
                            post.getId(),
                            post.getTitle(),
                            authorNickname,
                            0L,
                            0L,
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            author.isDeleted(),
                            false
                    );
                })
                .toList();
    }
    // 게시글 상세 조회
    public PostDetailResponse getPostDetail(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post_not_found"));
        if (post.isDeleted()){
            throw new IllegalArgumentException("post_not_found");
        }
        User author = userRepository.findById(post.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("No_User"));

        post.increaseViewCount();

        if (post.isBlinded()){
            return new PostDetailResponse(
                    post.getId(),
                    "블라인드 처리된 게시글입니다.",
                    null,
                    "블라인드 처리된 사용자입니다.",
                    null,
                    author.isDeleted(),
                    true,
                    post.getCreatedAt(),
                    null,
                    false,
                    null,
                    null,
                    null,
                    List.of()
            );
        }

        String authorNickname = author.isDeleted()
                ? "알 수 없음"
                : author.getNickname();

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                authorNickname,
                author.getProfileImage(),
                author.isDeleted(),
                false,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isEdited(),
                likeRepository.countByPostId(postId),
                (long) post.getViewCount(),
                commentRepository.countByPostId(postId),
                getComments(postId)
        );
    }
    public void deletePost(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("post_not_found"));

        post.delete();
    }
    public UpdatePostResponse updatePost(Long postId,UpdatePostRequest request){
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("No_Post"));
        User user = userRepository.findById(post.getAuthorId())
                .orElseThrow(()->new IllegalArgumentException("No_User"));
        post.update(request.getTitle(), request.getContent());

        return new UpdatePostResponse(
                postId,
                request.getTitle(),
                request.getContent(),
                user.getNickname(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.isEdited()
        );
    }

    // 상세 조회에 들어갈 댓글 목록 조회
    private List<CommentDetailResponse> getComments(Long postId) {
        return commentRepository.findParentCommentsByPostId(postId).stream()
                .map(comment -> {
                    User author = userRepository.findById(comment.getAuthorId())
                            .orElseThrow(() -> new IllegalArgumentException("No_User"));
                    return new CommentDetailResponse(
                            comment.getId(),
                            comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent(),
                            author.isDeleted() ? "알 수 없음" : author.getNickname(),
                            author.isDeleted(),
                            comment.isDeleted(),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt(),
                            getReplies(comment)
                    );
                })
                .toList();
    }

    // 댓글에 달린 대댓글 목록 조회
    private List<ReplyCreateResponse> getReplies(Comment parentComment) {
        return commentRepository.findRepliesByParentCommentId(parentComment.getId()).stream()
                .filter(reply -> !reply.isDeleted())
                .map(reply -> {
                    User author = userRepository.findById(reply.getAuthorId())
                            .orElseThrow(() -> new IllegalArgumentException("No_User"));
                    return new ReplyCreateResponse(
                            reply.getId(),
                            parentComment.getId(),
                            reply.getContent(),
                            author.isDeleted() ? "알 수 없음" : author.getNickname(),
                            author.isDeleted(),
                            reply.getCreatedAt()
                    );
                })
                .toList();
    }
}
