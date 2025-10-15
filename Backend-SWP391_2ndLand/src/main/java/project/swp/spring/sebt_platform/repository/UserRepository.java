package project.swp.spring.sebt_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.UserStatus;

import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findUserByEmail(String email);
    UserEntity findUserByUsername(String username);

    UserEntity findUserEntityById(Long id);


    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.role = project.swp.spring.sebt_platform.model.enums.UserRole.MEMBER " +
            "ORDER BY u.createdAt DESC ")
    Page<UserEntity> findAllMember(Pageable pageable);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.role = project.swp.spring.sebt_platform.model.enums.UserRole.MEMBER  " +
            "AND u.status = :status " +
            "ORDER BY u.createdAt DESC ")
    Page<UserEntity> findMemberByStatus(@Param("status") UserStatus status, Pageable pageable);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.role = project.swp.spring.sebt_platform.model.enums.UserRole.MEMBER  " +
            "AND (u.username LIKE %:keyword% OR u.email LIKE %:keyword%) " +
            "ORDER BY u.createdAt DESC ")
    Page<UserEntity> finMemberByUsernameContainingOrEmail(String keyword, Pageable pageable);

    //static method

    long countUserEntityByRole(UserRole role);

    @Query("""
    SELECT COUNT(u)
    FROM UserEntity u
    WHERE u.role = :role
      AND u.createdAt >= :sevenDaysAgo
""")
    long countUsersCreatedWithinLastWeek(
            @Param("role") UserRole role,
            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo
    );
}