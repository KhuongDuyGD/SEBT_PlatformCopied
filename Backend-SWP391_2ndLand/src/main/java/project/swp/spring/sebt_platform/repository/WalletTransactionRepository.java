package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.swp.spring.sebt_platform.model.WalletTransactionEntity;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {

    @Query("SELECT w FROM WalletTransactionEntity w WHERE w.OrderId = :orderId")
    WalletTransactionEntity findByOrderId(@Param("orderId") String orderId);
}
