package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.swp.spring.sebt_platform.model.WalletEntity;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    WalletEntity findByUserId(Long userId);
}
