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

        // authorization이 비어있거나 해당 값이 Bearer로 시작하지 않는다면 다음 filter 수행합니다..
        if( authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer하고 띄어쓰기 하나까지해서 7자이니간 앞에 삭제하고 token값 추출
        String token = authorization.substring(7);

        // 만약 토큰 검증에 실패하면 다음 filter 수행합니다.
        if(!jwtTokenProvider.validateToken(token)){
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtTokenProvider.getUserId(token);

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
