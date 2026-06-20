package kr.adapterz.springboot.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserJpaRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 테스트!!")
    void save_test(){
        User user = new User(
                "save@adapterz.kr",
                "1234",
                "saveUser",
                "profile.png"
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("save@adapterz.kr");
    }

    @Test
    void findById_Test(){
        User user = new User(
                "findid@adapterz.kr",
                "1234",
                "saveUser",
                "profile.png"
        );

        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("findid@adapterz.kr");
    }
}
