package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findUserByEmail(String email);
    UserEntity findUserByUsername(String username);
    UserEntity findUserEntityById(Long id);

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