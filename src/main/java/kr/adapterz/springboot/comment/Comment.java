package kr.adapterz.springboot.comment;

import jakarta.persistence.*;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_post_created_at", columnList = "post_id, created_at"),
                @Index(name = "idx_comments_parent_created_at", columnList = "parent_comment_id, created_at")
        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Comment(Post post, User author, Comment parentComment, String content) {
        this.post = post;
        this.author = author;
        this.parentComment = parentComment;
        this.content = content;
        this.deletedAt = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }
    // parentCommentId로 댓글과 대댓글 구분
    public boolean isReply() {
        return this.parentComment != null;
    }

    // 댓글 또는 대댓글 내용 수정
    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    // 댓글 또는 대댓글 삭제 처리
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
    public Boolean isDeleted(){
        return this.deletedAt != null;
    }
}
