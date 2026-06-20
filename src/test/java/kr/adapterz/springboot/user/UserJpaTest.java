package kr.adapterz.springboot.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void persist_실습() {
        User user = new User(
                "tester@adapterz.kr",
                "123aS!",
                "Adapterz",
                "profile.png"
        );

        System.out.println("persist 전 id = " + user.getId());

        entityManager.persist(user);

        System.out.println("persist 후 id = " + user.getId());

        System.out.println("=== flush 전 ===");

        entityManager.flush();

        System.out.println("=== flush 후 ===");
    }

    @Test
    void find_실습() {
        User user = new User(
                "tester@adapterz.kr",
                "123aS!",
                "Adapterz",
                "profile.png"
        );

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();
        User foundUser = entityManager.find(User.class, user.getId());

        System.out.println("found email = " + foundUser.getEmail());


    }
    @Test
    void first_cache_실습(){
        User user = new User(
                "tester@adapterz.kr",
                "123aS!",
                "Adapterz",
                "profile.png"
        );
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        System.out.println("=== 첫 번째 조회 ===");
        User user1 = entityManager.find(User.class, user.getId());

        System.out.println("=== 두 번째 조회 ===");
        User user2 = entityManager.find(User.class, user.getId());

        System.out.println("user1 == user2 : " + (user1 == user2));
    }

    @Test
    void dirty_checking_실습() {
        User user = new User(
                "dirty@adapterz.kr",
                "123aS!",
                "OldNick",
                "old.png"
        );

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = entityManager.find(User.class, user.getId());

        foundUser.updateProfile("NewNick", "new.png");

        entityManager.flush();
    }
    @Test
    void soft_delete_실습() {
        User user = new User(
                "delete@adapterz.kr",
                "123aS!",
                "DeleteUser",
                "profile.png"
        );

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = entityManager.find(User.class, user.getId());

        foundUser.delete();

        entityManager.flush();
    }
}
