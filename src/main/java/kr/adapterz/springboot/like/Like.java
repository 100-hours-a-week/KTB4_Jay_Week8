package kr.adapterz.springboot.like;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Fetch;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "likes",
        indexes = {
                @Index(name = "idx_likes_post_user", columnList = "`post_id`, `user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_likes_user_post"
                        , columnNames = {"user_id", "post_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Like(Post post, User user) {
        this.post = post;
        this.user = user;
    }
}
