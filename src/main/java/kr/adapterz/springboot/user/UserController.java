package kr.adapterz.springboot.user;

import jakarta.validation.Valid;
import kr.adapterz.springboot.global.ApiResponse;
import kr.adapterz.springboot.global.security.CustomUserPrincipal;
import kr.adapterz.springboot.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GetUserResponse>>> getUsers() {
        List<GetUserResponse> response = userService.getUsers();

        return ResponseEntity.ok(
                new ApiResponse<>("user_list_success", response)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> getMe(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ){
        UserMeResponse response = userService.getMe(customUserPrincipal.getUserId());

        return ResponseEntity.ok(
                new ApiResponse<>("user_me_success", response)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ){
        RegisterResponse response = userService.register(request);
        //  new RegisterResponse(savedUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("register_success", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ){
        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(
                new ApiResponse<>("login_success", response)
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @RequestBody UserUpdateRequest request
    ){
        UserUpdateResponse response = userService.update(
                customUserPrincipal.getUserId(),
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>("user_information_update_success", response)
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal
    ){
        userService.deleteUser(customUserPrincipal.getUserId());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPass(
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal,
            @RequestBody UserUpdatePassRequest request
    ){
        userService.updatePass(
                customUserPrincipal.getUserId(),
                request
        );

        return ResponseEntity.ok(new ApiResponse<>("change_password_success", null));
    }
}
