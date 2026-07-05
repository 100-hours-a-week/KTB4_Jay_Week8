package kr.adapterz.springboot.auth;


import kr.adapterz.springboot.auth.dto.TokenRefreshRequest;
import kr.adapterz.springboot.auth.dto.TokenRefreshResponse;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.global.security.JwtTokenProvider;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("empty_refresh_token");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("invalid_refresh_token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("invalid_refresh_token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("invalid_refresh_token"));

        if (user.isDeleted()) {
            throw new UnauthorizedException("invalid_refresh_token");
        }

        RefreshToken savedToken = refreshTokenRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UnauthorizedException("refresh_token_not_found"));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new UnauthorizedException("invalid_refresh_token");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail()
        );

        return new TokenRefreshResponse(newAccessToken);
    }
}