package kr.adapterz.springboot.user;

import kr.adapterz.springboot.global.security.JwtTokenProvider;
import kr.adapterz.springboot.user.dto.LoginRequest;
import kr.adapterz.springboot.user.dto.LoginResponse;
import kr.adapterz.springboot.user.dto.RegisterRequest;
import kr.adapterz.springboot.user.dto.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

// 실제로 통합테스트를 위해서 springBootTest 어노테이션을 붙여줍니다.
@SpringBootTest
// 진짜 commit 해버리면 안되니깐 transactional을 붙여줍니다.
@Transactional
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private RegisterResponse registeredUser;

    @BeforeEach
    void setUp(){
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        );

        registeredUser = userService.register(request);
    }
    @Test
    @DisplayName("회원가입에 성공했다!!")
    void 회원가입_성공(){
        // when & then
        User savedUser = userRepository.findById(registeredUser.getUserId())
                .orElseThrow();

        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("test_nickname");
        assertThat(savedUser.isDeleted()).isFalse();
        assertThat(savedUser.getPassword()).isNotEqualTo("password1234!");
        assertThat(passwordEncoder.matches("password1234!",savedUser.getPassword())).isTrue();
    }

    @Test
    void 로그인_성공() {

        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password1234!"
        );

        // when
        LoginResponse response = userService.login(request);

        // then
        User foundUser = userRepository.findById(response.getUserId())
                .orElseThrow();

        assertThat(foundUser.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getUserId()).isEqualTo(registeredUser.getUserId());

        // 진짜 accessToken을 변수로 저장
        String accessToken = response.getAccessToken();

        // 해당 accessToken이 validate한지 검증
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();

        // 토큰에서 id 뽑아내서 맞는지 검증
        Long userIdFromToken = jwtTokenProvider.getUserId(accessToken);
        assertThat(userIdFromToken).isEqualTo(registeredUser.getUserId());

        // refreshToken도 비어있지 않은지 확인
        assertThat(response.getRefreshToken()).isNotBlank();
    }
}
