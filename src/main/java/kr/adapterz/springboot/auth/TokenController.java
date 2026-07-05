package kr.adapterz.springboot.auth;

import kr.adapterz.springboot.auth.dto.TokenRefreshRequest;
import kr.adapterz.springboot.auth.dto.TokenRefreshResponse;
import kr.adapterz.springboot.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/token")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
            @RequestBody TokenRefreshRequest request
    ) {
        TokenRefreshResponse response = tokenService.refreshAccessToken(request);

        return ResponseEntity.ok(
                new ApiResponse<>("token_refresh_success", response)
        );
    }
}