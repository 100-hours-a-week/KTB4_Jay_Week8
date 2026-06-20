package kr.adapterz.springboot.post;

import jakarta.persistence.*;
import kr.adapterz.springboot.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "index_posts_authorId_createdAt", columnList = "author_id,created_at"),
                @Index(name = "index_posts_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private boolean blinded;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private boolean edited;

    public Post(User author, String title, String content){
        this.author = author;
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
        this.blinded = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
        this.deletedAt = null;
        this.edited = false;
    }

    public void increaseViewCount(){
        this.viewCount ++;
    }

    // 신고 누적 5번 이상이면 blind 함수 실행
    public void blind(){
        this.blinded = true;
    }

    // 게시글 수정 시 제목과 내용 변경
    public void update(String title, String content){
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
        this.edited = true;
    }

    public void delete(){
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted(){
        return this.deletedAt != null;
    }

}
