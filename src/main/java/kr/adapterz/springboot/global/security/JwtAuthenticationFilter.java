package kr.adapterz.springboot.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.adapterz.springboot.user.User;
import kr.adapterz.springboot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Header에서 Authorization 값만 뽑아내기
        String authorization = request.getHeader("Authorization");

        // 헤더가 없거나 Bearer로 시작하지 않으면 통과하고 다음 필터 실행
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 여기서부터는 토큰 있는 놈들만 걸러짐. 앞에서 7자 빼고 진짜 토큰만 token에 저장
        String token = authorization.substring(7);

        // 토큰 만료 확인하고 유효한지 확인하기
        if (jwtTokenProvider.isExpiredToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"expired_access_token\",\"data\":null}");
            return;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"invalid_access_token\",\"data\":null}");
            return;
        }
        // token 타입도 확인
        String tokenType = jwtTokenProvider.getTokenType(token);

        if (!"access".equals(tokenType)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"invalid_access_token\",\"data\":null}");
            return;
        }
        // 이제 토큰으로 userId를 찾기
        Long userId = jwtTokenProvider.getUserId(token);

        // 이후 User 조회, principal 생성, Authentication 저장
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null || user.isDeleted()) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);

    }
}
