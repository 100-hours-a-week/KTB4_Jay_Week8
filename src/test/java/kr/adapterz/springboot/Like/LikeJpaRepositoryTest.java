package kr.adapterz.springboot.Like;

import kr.adapterz.springboot.like.LikeRepository;
import kr.adapterz.springboot.post.Post;
import kr.adapterz.springboot.post.PostRepository;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import kr.adapterz.springboot.like.Like;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LikeJpaRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    void unlike_test(){
        // 유저 생성
        User user = new User(
                "save@adapterz.kr",
                "1234",
                "saveUser",
                "profile.png"
        );
        // 유저 저장
        User savedUser = userRepository.save(user);

        // 게시글 생성
        Post post = new Post(
                savedUser,
                "testtitle",
                "testcontent"
        );
        //post 저장
        Post savedPost = postRepository.save(post);

        Like like = new Like(
                savedPost,
                savedUser
        );

        Like like1 = new Like(
                savedPost,
                savedUser
        );

        likeRepository.save(like);
        likeRepository.save(like1);
        System.out.println("like 저장 완료!");
        assertThat(like.getId()).isNotNull();


        likeRepository.delete(like);
        likeRepository.flush();
        System.out.println("like 삭제 완료!");

        assertThat(likeRepository.count()).isEqualTo(0);

    }
}