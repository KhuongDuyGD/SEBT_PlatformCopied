package project.swp.spring.sebt_platform.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;

import java.util.Optional;

@Repository
public interface PostRequestRepository extends JpaRepository<PostRequestEntity, Long> {

    PostRequestEntity findPostRequestEntitiesById(Long id);

    Page<PostRequestEntity> findAllBy(Pageable pageable);

    Page<PostRequestEntity> findAllByStatusIs(ApprovalStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE PostRequestEntity p SET p.status = 'APPROVED', p.reviewedAt = CURRENT_TIMESTAMP WHERE p.id = :requestId")
    void approvePostRequest(@Param("requestId") Long requestId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE PostRequestEntity p SET p.status = 'REJECTED', p.reviewedAt = CURRENT_TIMESTAMP, p.adminNotes = :reason WHERE p.id = :requestId")
    void rejectPostRequest(@Param("requestId") Long requestId, @Param("reason") String reason);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE PostRequestEntity p SET p.status = 'REQUIRES_CHANGES', p.reviewedAt = CURRENT_TIMESTAMP, p.adminNotes = :reason WHERE p.id = :requestId")
    void requireChangeRequest(@Param("requestId") Long requestId, @Param("reason") String reason);
}

