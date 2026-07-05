package kr.adapterz.springboot.user;

import kr.adapterz.springboot.global.exception.BadRequestException;
import kr.adapterz.springboot.global.exception.ConflictException;
import kr.adapterz.springboot.global.exception.UnauthorizedException;
import kr.adapterz.springboot.user.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // ==============================
    // 회원가입
    // ==============================
    @Test
    @DisplayName("회원가입에 성공했다!!")
    void 회원가입_성공(){
        //given
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        );

        //when
        RegisterResponse response = userService.register(request);

        //then
        User savedUser = userRepository.findById(response.getUserId())
                .orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("test_nickname");
        assertThat(savedUser.isDeleted()).isFalse();
    }

    @Test
    void 이미_존재하는_이메일이면_회원가입_실패(){
        // given
        // 한 계정 회원가입
        userService.register(new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        ));
        // 같은 이메일의 계정 준비
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname1",
                "profile_image"
        );

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void 이미_존재하는_닉네임이면_회원가입_실패(){
        // given
        // 한 계정 회원가입
        userService.register(new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        ));
        // 같은 이메일의 계정 준비
        RegisterRequest request = new RegisterRequest(
                "test@test2.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        );

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    // ==============================
    // 로그인
    // ==============================

    @Test
    void 로그인_성공() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test1234!"
        );

        // when
        LoginResponse response = userService.login(request);

        // then
        User foundUser = userRepository.findById(response.getUserId())
                .orElseThrow();

        assertThat(foundUser.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getUserId()).isEqualTo(registerResponse.getUserId());
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }
    @Test
    void 탈퇴한_사용자인_경우_로그인_실패() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        userService.deleteUser(registerResponse.getUserId());

        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test1234!"
        );

        // when & then
        assertThatThrownBy(()-> userService.login(request))
                .isInstanceOf(UnauthorizedException.class);


    }
    @Test
    void 비밀번호가_다른_경우_로그인하면_실패(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        LoginRequest request = new LoginRequest(
                "test@test.com",
                "test5678!"
        );

        // when & then
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class);

    }

    // ==============================
    // 내 정보 불러오기
    // ==============================
    @Test
    @DisplayName("내 정보 불러오기 성공입니다.")
    void 내정보_불러오기(){
        //given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "password1234!",
                "test_nickname",
                "profile_image"
        ));

        Long userId = registerResponse.getUserId();

        //when
        UserMeResponse response = userService.getMe(userId);

        // then
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getNickname()).isEqualTo("test_nickname");
        assertThat(response.getProfileImage()).isEqualTo("profile_image");
    }

    @Test
    void 존재하지_않는_사용자면_내정보_조회에_실패한다() {
        // given
        Long invalidUserId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.getMe(invalidUserId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 탈퇴한_사용자면_내정보_조회에_실패한다() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "password1234",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        userService.deleteUser(userId);

        // when & then
        assertThatThrownBy(() -> userService.getMe(userId))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ==============================
    // 회원정보 수정
    // ==============================

    @Test
    void 회원정보_수정_완료(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "jay",
                "profile_jay"
        ));
        UserUpdateRequest request = new UserUpdateRequest(
                "jason",
                "profile_jason"
        );

        // when
        UserUpdateResponse response = userService.update(registerResponse.getUserId(), request);

        // then
        assertThat(response.getNickname()).isEqualTo("jason");

    }
    @Test
    void 닉네임_비어있으면_수정실패(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "jay",
                "profile_jay"
        ));

        UserUpdateRequest request = new UserUpdateRequest(
                "",
                "profile_jason"
        );

        // when & then
        assertThatThrownBy(()-> userService.update(registerResponse.getUserId() , request))
                .isInstanceOf(BadRequestException.class);
    }
    @Test
    void 프로필_비어있으면_수정실패(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "jay",
                "profile_jay"
        ));

        UserUpdateRequest request = new UserUpdateRequest(
                "jason",
                ""
        );

        // when & then
        assertThatThrownBy(()-> userService.update(registerResponse.getUserId() , request))
                .isInstanceOf(BadRequestException.class);

    }
    @Test
    void 프로필_null이면_수정실패(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "jay",
                "profile_jay"
        ));

        UserUpdateRequest request = new UserUpdateRequest(
                "jason",
                null
        );

        // when & then
        assertThatThrownBy(()-> userService.update(registerResponse.getUserId() , request))
                .isInstanceOf(BadRequestException.class);

    }
    @Test
    void 닉네임_중복되면_수정실패(){
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "jay",
                "profile_jay"
        ));
        userService.register(new RegisterRequest(
                "test@test1.com",
                "test5678!",
                "jin",
                "profile_jin"
        ));

        UserUpdateRequest request = new UserUpdateRequest(
                "jin",
                "profile_jason"
        );

        // when & then
        assertThatThrownBy(()-> userService.update(registerResponse.getUserId() , request))
                .isInstanceOf(ConflictException.class);

    }
    // ==============================
    // 회원정보 비밀번호 수정
    // ==============================

    @Test
    void 비밀번호_변경_성공() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "test1234!",
                "new1234!",
                "new1234!"
        );

        // when
        userService.updatePass(userId, request);

        // then
        User foundUser = userRepository.findById(userId)
                .orElseThrow();

        assertThat(passwordEncoder.matches("new1234!", foundUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("test1234!", foundUser.getPassword())).isFalse();
    }
    @Test
    void 현재_비밀번호가_틀리면_비밀번호_변경_실패() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "wrong1234!",
                "new1234!",
                "new1234!"
        );

        // when & then
        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }
    @Test
    void 새_비밀번호와_확인값이_다르면_비밀번호_변경_실패() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "test1234!",
                "new1234!",
                "different1234!"
        );

        // when & then
        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }
    @Test
    void 탈퇴한_사용자는_비밀번호_변경_실패() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        userService.deleteUser(userId);

        UserUpdatePassRequest request = new UserUpdatePassRequest(
                "test1234!",
                "new1234!",
                "new1234!"
        );

        // when & then
        assertThatThrownBy(() -> userService.updatePass(userId, request))
                .isInstanceOf(BadRequestException.class);
    }
    // ==============================
    // 회원 탈퇴
    // ==============================
    @Test
    void 회원탈퇴_시_삭제_상태로_변경() {
        // given
        RegisterResponse registerResponse = userService.register(new RegisterRequest(
                "test@test.com",
                "test1234!",
                "테스터",
                null
        ));

        Long userId = registerResponse.getUserId();

        // when
        userService.deleteUser(userId);

        // then
        User foundUser = userRepository.findById(userId)
                .orElseThrow();

        assertThat(foundUser.isDeleted()).isTrue();
        assertThat(foundUser.getEmail()).startsWith("deleted_" + userId + "_");
        assertThat(foundUser.getNickname()).isEqualTo("deleted_user_" + userId);
    }
}
