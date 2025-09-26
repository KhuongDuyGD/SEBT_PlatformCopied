package project.swp.spring.sebt_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.swp.spring.sebt_platform.model.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
