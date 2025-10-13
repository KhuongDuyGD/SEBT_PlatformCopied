package project.swp.spring.sebt_platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import project.swp.spring.sebt_platform.model.EvVehicleEntity;

@Repository
public interface EvVehicleRepository extends JpaRepository<EvVehicleEntity, Long> {
    
    /**
     * Lấy danh sách các thương hiệu xe điện duy nhất (distinct) để dùng cho filter dropdown
     * @return Danh sách thương hiệu xe điện sắp xếp theo thứ tự ABC
     */
    @Query("SELECT DISTINCT ev.brand FROM EvVehicleEntity ev ORDER BY ev.brand ASC")
    List<String> findDistinctBrands();
    
    /**
     * Lấy danh sách năm sản xuất duy nhất để dùng cho filter dropdown
     * @return Danh sách năm sản xuất sắp xếp giảm dần (mới nhất trước)
     */
    @Query("SELECT DISTINCT ev.year FROM EvVehicleEntity ev ORDER BY ev.year DESC")
    List<Integer> findDistinctYears();
}
