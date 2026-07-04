package kr.adapterz.springboot.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdatePassRequest {
    private String newPassword;
    private String newPasswordCheck;
}
