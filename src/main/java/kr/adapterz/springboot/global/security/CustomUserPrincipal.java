package kr.adapterz.springboot.global.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal {

    private Long userId;
    private String email;
    private String nickname;
}