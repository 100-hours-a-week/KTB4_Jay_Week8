package kr.adapterz.springboot.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                // User 테이블에서 UNIQUE 설정은 email, nickname 두 개에 들어가 있음
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public User(String email, String password, String nickname, String profileImage){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.deletedAt = null;
    }

    // 회원정보(닉네임, 프로필 이미지) 수정
    public void updateProfile(String nickname, String profileImage){
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    //비밀번호 변경
    public void changePassword(String newPassword){
        this.password = newPassword;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.email = "deleted_" + this.id + "_" + this.email;
        this.nickname = "deleted_user_" + this.id;
    }

    public boolean isDeleted(){
        return this.deletedAt != null;
    }
}
