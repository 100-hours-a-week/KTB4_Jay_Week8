package kr.adapterz.springboot.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMeResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String profileImage;
}
