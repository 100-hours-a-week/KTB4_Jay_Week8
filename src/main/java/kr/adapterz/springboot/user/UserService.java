package kr.adapterz.springboot.user;

import org.springframework.transaction.annotation.Transactional;
import kr.adapterz.springboot.global.exception.UserNotFoundException;
import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

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
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()){
            throw new BadRequestException("empty_email");
        }
        if (request.getNickname() == null || request.getNickname().isBlank()){
            throw new BadRequestException("empty_nickname");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()){
            throw new BadRequestException("empty_password");
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new ConflictException("already_exist_email");
        }
        if (userRepository.existsByNickname(request.getNickname())){
            throw new ConflictException("already_exist_nickname");
        }
        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                request.getProfileImage()
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId());
    }

    public LoginResponse login(LoginRequest request){
        if (request.getEmail() == null || request.getEmail().isBlank()){
            throw new BadRequestException("empty_email");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()){
            throw new BadRequestException("empty_password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("login_failed"));

        if(!user.getPassword().equals(request.getPassword())){
            throw new UnauthorizedException("login_failed");
        }

        return new LoginResponse(
                "fake-user-Token_" + user.getId(),
                user.getId()
        );
    }

    @Transactional
    public UserUpdateResponse update(UserUpdateRequest request){
        if (request.getNickname() == null || request.getNickname().isBlank()){
            throw new BadRequestException("empty_nickname");
        }
        if (request.getProfileImage() == null || request.getProfileImage().isBlank()){
            throw new BadRequestException("empty_profileImage");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (!user.getNickname().equals(request.getNickname())
                && userRepository.existsByNickname(request.getNickname())) {
            throw new ConflictException("already_exist_nickname");
        }

        user.updateProfile(request.getNickname(), request.getProfileImage());

        return new UserUpdateResponse(
                user.getNickname(),
                user.getProfileImage()
        );
    }
    @Transactional
    public void deleteUser(UserDeleteRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        user.delete();
    }
    @Transactional
    public void updatePass(UserUpdatePassRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        if (user.isDeleted()){
            throw new BadRequestException("deleted_user");
        }
        if (request.getNewpassword() == null || request.getNewpassword().isBlank()) {
            throw new BadRequestException("empty_password");
        }
        if (!request.getNewpassword().equals(request.getNewpasswordCheck())) {
            throw new BadRequestException("password_check_not_match");
        }
        user.changePassword(request.getNewpassword());
    }
}
