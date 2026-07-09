package com.kbt.amumal.domain.user.repository;

import com.kbt.amumal.domain.user.dto.UserProjection;
import com.kbt.amumal.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByUserId(String userId);
    List<User> findByDeletedAtIsNotNullAndDeletedAtBefore(LocalDateTime cutoff);

    // N+1 방지: 여러 유저 정보를 DTO Projection으로 한 번에 조회
    @Query("SELECT new com.kbt.amumal.domain.user.dto.UserProjection(u.id, u.userId, u.nickname, u.profileImageUrl) FROM User u WHERE u.id IN :ids")
    List<UserProjection> findProjectionsByIdIn(@Param("ids") List<Integer> ids);
}