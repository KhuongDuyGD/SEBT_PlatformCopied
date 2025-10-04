package project.swp.spring.sebt_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;

@Repository
public interface PostRequestRepository extends JpaRepository<PostRequestEntity, Long> {

    Page<PostRequestEntity> findAllBy(Pageable pageable);

    Page<PostRequestEntity> findAllByStatusIs(ApprovalStatus status, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE PostRequestEntity p SET p.status = 'APPROVED', p.reviewedAt = CURRENT_TIMESTAMP WHERE p.id = :requestId")
    void approvePostRequest(Long requestId);

    @Transactional
    @Modifying
    @Query("UPDATE PostRequestEntity p SET p.status = 'REJECTED', p.reviewedAt = CURRENT_TIMESTAMP, p.adminNotes = :reason WHERE p.id = :requestId")
    void rejectPostRequest(Long requestId, String reason);
}
