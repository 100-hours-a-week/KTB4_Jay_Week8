package kr.adapterz.springboot.user;

import kr.adapterz.springboot.auth.RefreshToken;
import kr.adapterz.springboot.auth.RefreshTokenRepository;
import kr.adapterz.springboot.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserReader userReader;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public List<GetUserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new GetUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getProfileImage(),
                        user.isDeleted()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("invalid_user"));

        if (user.isDeleted()) {
            throw new UnauthorizedException("deleted_user");
        }

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImage()
        );
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())){
            throw new ConflictException("already_exist_email");
        }
        if (userRepository.existsByNicknameAndDeletedAtIsNull(request.getNickname())){
            throw new ConflictException("already_exist_nickname");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getNickname(),
                request.getProfileImage()
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId());
    }

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("login_failed"));

        if (user.isDeleted()){
            throw new UnauthorizedException("deleted_user");
        }

        boolean passwordMatched = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!passwordMatched){
            throw new UnauthorizedException("login_failed");
        }


        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId()
        );

        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUser_Id(user.getId())
                .orElse(null);

        if (savedRefreshToken == null){
            refreshTokenRepository.save(new RefreshToken(user, refreshToken, refreshTokenExpiresAt));
        } else {
            savedRefreshToken.updateToken(refreshToken, refreshTokenExpiresAt);
        }

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getId()
        );
    }

    @Transactional
    public UserUpdateResponse update(
            Long currentUserId,
            UserUpdateRequest request
    ){
        if (request.getNickname() == null || request.getNickname().isBlank()){
            throw new BadRequestException("empty_nickname");
        }
        if (request.getProfileImage() == null || request.getProfileImage().isBlank()){
            throw new BadRequestException("empty_profileImage");
        }
        User user = userReader.getUser(currentUserId);

        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNicknameAndDeletedAtIsNull(request.getNickname())) {
            throw new ConflictException("already_exist_nickname");
        }

        user.updateProfile(request.getNickname(), request.getProfileImage());

        return new UserUpdateResponse(
                user.getNickname(),
                user.getProfileImage()
        );
    }

    @Transactional
    public void deleteUser(Long currentUserId) {

        User user = userReader.getUser(currentUserId);

        user.delete();
    }

    @Transactional
    public void updatePass(
            Long currentUserId,
            UserUpdatePassRequest request
    ){
        User user = userReader.getUser(currentUserId);

        if (user.isDeleted()){
            throw new BadRequestException("deleted_user");
        }
        boolean currentPasswordMatched = passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        );

        if (!currentPasswordMatched){
            throw new BadRequestException("wrong_current_password");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new BadRequestException("empty_password");
        }
        if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
            throw new BadRequestException("password_check_not_match");
        }
        String encodedPass = passwordEncoder.encode(request.getNewPassword());
        user.changePassword(encodedPass);
    }
}
