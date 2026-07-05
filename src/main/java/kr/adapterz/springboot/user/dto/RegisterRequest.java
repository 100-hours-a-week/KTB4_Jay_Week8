//회원가입 요청 dto

package kr.adapterz.springboot.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "empty_email")
    private String email;

    @NotBlank(message = "empty_password")
    private String password;

    @NotBlank(message = "empty_nickname")
    private String nickname;

    private String profileImage;
}
