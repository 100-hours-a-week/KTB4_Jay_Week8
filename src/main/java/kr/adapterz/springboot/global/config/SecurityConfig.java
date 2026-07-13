package kr.adapterz.springboot.global.config;

import kr.adapterz.springboot.global.security.JwtAuthenticationFilter;
import kr.adapterz.springboot.global.security.handler.CustomAccessDeniedHandler;
import kr.adapterz.springboot.global.security.handler.CustomAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // WebConfig에서 CORS를 열어주긴 하지만 더 앞단에서 동작하기 때문에 해당 파일에서도 CORS 열어줘야됨
                .cors(cors -> {})
                // CSRF는 주로 세션-쿠키 방식에서 일어나는 공격 방식이라 REST API 방식에서는 끄고 시작하는게 일반적임
                .csrf(AbstractHttpConfigurer::disable)
                // formLogin 필터가 처리하는 로그인이 아니므로 disable 처리하고 시작
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic은 요청 헤더에 아이디/비밀번호를 실어 보내는 방식인데 JWT를 쓸 것이기 때문에 이것도 disable 처리
                .httpBasic(AbstractHttpConfigurer::disable)
                // JWT 방식에서는 상태를 서버 세션에 저장하지 않는 쪽으로 구성합니다.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                // authorization filter가 나중에 사용할 규칙표 입니다.
                // OPTIONS, 회원가입, 로그인은 막지 않고 게시글 상세조회 목록도 로그인 하지 않은 상태로 볼 수 있습니다.
                // 그 외의 요청은 모두 인가 검사를 합니다.
                .authorizeHttpRequests(
                        auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/users/register", "/users/login", "/users/token/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().hasRole("USER")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}