package kr.adapterz.springboot.post;

import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostJpaRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void post_save(){
        User user = new User(
                "save@adapterz.kr",
                "1234",
                "saveUser",
                "profile.png"
        );

        User savedUser = userRepository.save(user);

        Post post = new Post(
                savedUser,
                "testtitle",
                "testcontent"
        );

        Post savedPost = postRepository.save(post);

        assertThat(savedPost.getId()).isNotNull();
    }
}
