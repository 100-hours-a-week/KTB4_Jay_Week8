package kr.adapterz.springboot.auth;

import kr.adapterz.springboot.auth.dto.TokenRefreshRequest;
import kr.adapterz.springboot.auth.dto.TokenRefreshResponse;
import kr.adapterz.springboot.global.security.JwtTokenProvider;
import kr.adapterz.springboot.user.UserService;
import kr.adapterz.springboot.user.dto.LoginRequest;
import kr.adapterz.springboot.user.dto.LoginResponse;
import kr.adapterz.springboot.user.dto.RegisterRequest;
import kr.adapterz.springboot.user.dto.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TokenServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private RegisterResponse registeredUser;

    @BeforeEach
    void setUp() {
        registeredUser = userService.register(new RegisterRequest(
                "token@test.com",
                "password1234!",
                "토큰유저",
                null
        ));
    }

    // ==============================
    // access token 재발급 통합 흐름
    // ==============================

    @Test
    void 저장된_refreshToken으로_accessToken을_재발급한다() {
        LoginResponse loginResponse = userService.login(new LoginRequest(
                "token@test.com",
                "password1234!"
        ));

        TokenRefreshResponse response = tokenService.refreshAccessToken(
                refreshRequest(loginResponse.getRefreshToken())
        );

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(response.getAccessToken())).isTrue();
        assertThat(jwtTokenProvider.getUserId(response.getAccessToken())).isEqualTo(registeredUser.getUserId());
    }

    private TokenRefreshRequest refreshRequest(String refreshToken) {
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);
        return request;
    }
}
