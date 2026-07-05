package kr.adapterz.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePassRequest {
    private String currentPassword;
    private String newPassword;
    private String newPasswordCheck;
}
