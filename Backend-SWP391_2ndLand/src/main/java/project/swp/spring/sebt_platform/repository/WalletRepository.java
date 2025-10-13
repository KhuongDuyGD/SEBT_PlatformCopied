package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.swp.spring.sebt_platform.model.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    WalletEntity findByUserId(Long userId);

    // Find wallet by a transaction orderId (joins wallet -> transactions)
    @Query("""
        SELECT w FROM WalletEntity w 
        JOIN w.transactions wt  
        WHERE wt.orderId = :orderId
       """)
    WalletEntity findByOrderId(@Param("orderId") String orderId);

}
