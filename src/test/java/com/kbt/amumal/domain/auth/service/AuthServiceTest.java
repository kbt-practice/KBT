package com.kbt.amumal.domain.auth.service;

import com.kbt.amumal.domain.auth.dto.AuthReqDTO;
import com.kbt.amumal.domain.user.entity.User;
import com.kbt.amumal.domain.user.repository.UserRepository;
import com.kbt.amumal.global.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인에 성공하면 토큰을 발급한다")
    void login_success() {
        // given
        ReflectionTestUtils.setField(authService, "refreshExpTimeMs", 2592000000L);

        User user = User.builder()
                .id(1)
                .email("song@test.com")
                .password("encoded-password")
                .nickname("song")
                .build();
        AuthReqDTO.LoginReq request = new AuthReqDTO.LoginReq("song@test.com", "Pw1234!!");

        given(userRepository.findByEmail(request.email()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword()))
                .willReturn(true);
        given(jwtUtil.createAccessToken(user.getId(), user.getEmail()))
                .willReturn("access-token");
        given(jwtUtil.createRefreshToken(user.getId(), user.getEmail()))
                .willReturn("refresh-token");
        given(redisTemplate.opsForValue())
                .willReturn(valueOperations);

        // when
        AuthService.LoginResult result = authService.userLogin(request);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }
}
