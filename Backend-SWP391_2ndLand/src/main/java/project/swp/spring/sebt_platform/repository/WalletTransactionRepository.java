package project.swp.spring.sebt_platform.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;
import project.swp.spring.sebt_platform.model.enums.WalletPurpose;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {

    @Query("SELECT w FROM WalletTransactionEntity w WHERE w.orderId = :orderId")
    WalletTransactionEntity findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT w FROM WalletTransactionEntity w WHERE w.userId = :userId ORDER BY w.createdAt DESC")
    Page<WalletTransactionEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w FROM WalletTransactionEntity w WHERE w.userId = :userId AND w.purpose = :purpose ORDER BY w.createdAt DESC")
    Page<WalletTransactionEntity> findByUserIdAndPurposeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("purpose") WalletPurpose purpose, Pageable pageable);
}
