package kr.adapterz.springboot.auth;

import kr.adapterz.springboot.auth.dto.TokenRefreshRequest;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.global.security.JwtTokenProvider;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenServiceUnitTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    // ==============================
    // refresh token 입력 검증
    // ==============================

    @Test
    void refreshToken이_비어있으면_재발급_실패() {
        assertThatThrownBy(() -> tokenService.refreshAccessToken(refreshRequest("")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("empty_refresh_token");
    }

    // ==============================
    // 저장된 refresh token 검증
    // ==============================

    @Test
    void 저장된_refreshToken과_다르면_재발급_실패() {
        String refreshToken = "refresh-token";
        Long userId = 1L;
        User user = user(userId);
        RefreshToken savedToken = new RefreshToken(user, "different-token", LocalDateTime.now().plusDays(7));

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getTokenType(refreshToken)).willReturn("refresh");
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(refreshTokenRepository.findByUser_Id(userId)).willReturn(Optional.of(savedToken));

        assertThatThrownBy(() -> tokenService.refreshAccessToken(refreshRequest(refreshToken)))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("invalid_refresh_token");
    }

    private TokenRefreshRequest refreshRequest(String refreshToken) {
        return new TokenRefreshRequest(refreshToken);
    }

    private User user(Long id) {
        User user = new User("token@test.com", "encoded-password", "토큰유저", null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
