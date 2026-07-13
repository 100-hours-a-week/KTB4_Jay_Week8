package kr.adapterz.springboot.user;

import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.user.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private PasswordEncoder passwordEncoder;

    // ==============================
    // 회원가입
    // ==============================

    @Test
    void 이미_존재하는_이메일이면_회원가입_실패(){
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname1",
                "profile_image"
        );
        given(userRepository.existsByEmailAndDeletedAtIsNull("test@test.com")).willReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("already_exist_email");
    }

    @Test
    void 이미_존재하는_닉네임이면_회원가입_실패(){
        RegisterRequest request = new RegisterRequest(
                "test@test2.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        );
        given(userRepository.existsByEmailAndDeletedAtIsNull("test@test2.com")).willReturn(false);
        given(userRepository.existsByNicknameAndDeletedAtIsNull("test_nickname")).willReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("already_exist_nickname");
    }

    // ==============================
    // 로그인
    // ==============================

    @Test
    void 존재하지_않는_이메일인_경우_로그인_실패() {
        LoginRequest request = new LoginRequest(
                "wrong@test.com",
                "test1234!"
        );
        given(userRepository.findByEmail("wrong@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("login_failed");
    }

    @Test
    void 탈퇴한_사용자인_경우_로그인_실패() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test1234!"
        );
        User mockUser = new User("test@test.com", "encoded_pass", "테스터", null);
        mockUser.delete();
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));

        assertThatThrownBy(()-> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("deleted_user");
    }

    @Test
    void 비밀번호가_다른_경우_로그인하면_실패(){
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test5678!"
        );
        User mockUser = new User("test@test.com", "encoded_test1234!", "테스터", null);
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("test5678!", "encoded_test1234!")).willReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("login_failed");
    }

    // ==============================
    // 내 정보 불러오기
    // ==============================

    @Test
    void 존재하지_않는_사용자면_내정보_조회에_실패한다() {
        Long invalidUserId = 999L;
        given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(invalidUserId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 탈퇴한_사용자면_내정보_조회에_실패한다() {
        Long userId = 1L;
        User mockUser = new User("test@test.com", "password1234", "테스터", null);
        mockUser.delete();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> userService.getMe(userId))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ==============================
    // 회원정보 수정
    // ==============================

    @Test
    void 닉네임_비어있으면_수정실패(){
        UserUpdateRequest request = new UserUpdateRequest(
                "",
                "profile_jason"
        );

        assertThatThrownBy(()-> userService.update(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로필_비어있으면_수정실패(){
        UserUpdateRequest request = new UserUpdateRequest(
                "jason",
                ""
        );

        assertThatThrownBy(()-> userService.update(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 프로필_null이면_수정실패(){
        UserUpdateRequest request = new UserUpdateRequest(
                "jason",
                null
        );

        assertThatThrownBy(()-> userService.update(1L, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 닉네임_중복되면_수정실패(){
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "jin",
                "profile_jason"
        );
        User mockUser = new User("test@test.com", "test1234!", "jay", "profile_jay");
        given(userReader.getUser(userId)).willReturn(mockUser);
        given(userRepository.existsByNicknameAndDeletedAtIsNull("jin")).willReturn(true);

        assertThatThrownBy(()-> userService.update(userId, request))
                .isInstanceOf(ConflictException.class);
    }

    // ==============================
    // 회원정보 비밀번호 수정
    // ==============================

    @Test
    void 현재_비밀번호가_틀리면_비밀번호_변경_실패() {
        Long userId = 1L;
        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "wrong1234!",
                "new1234!",
                "new1234!"
        );
        User mockUser = new User("test@test.com", "encoded_test1234!", "테스터", null);
        given(userReader.getUser(userId)).willReturn(mockUser);
        given(passwordEncoder.matches("wrong1234!", "encoded_test1234!")).willReturn(false);

        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 새_비밀번호와_확인값이_다르면_비밀번호_변경_실패() {
        Long userId = 1L;
        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "test1234!",
                "new1234!",
                "different1234!"
        );
        User mockUser = new User("test@test.com", "encoded_test1234!", "테스터", null);
        given(userReader.getUser(userId)).willReturn(mockUser);
        given(passwordEncoder.matches("test1234!", "encoded_test1234!")).willReturn(true);

        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void 탈퇴한_사용자는_비밀번호_변경_실패() {
        Long userId = 1L;
        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "test1234!",
                "new1234!",
                "new1234!"
        );
        User mockUser = new User("test@test.com", "test1234!", "테스터", null);
        mockUser.delete();
        given(userReader.getUser(userId)).willReturn(mockUser);

        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }
}