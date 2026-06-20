package kr.adapterz.springboot.report;

import jakarta.persistence.*;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "report",
        indexes = {
                @Index(name = "idx_reports_post_created_at"
                        ,columnList = "post_id, created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reports_user_post",
                columnNames = {"user_id","post_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Report(Post post, User user, String reason) {
        this.post = post;
        this.user = user;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }
}
